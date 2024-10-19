﻿function uuidv4() {
    return "10000000-1000-4000-8000-100000000000".replace(/[018]/g, c =>
        (+c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> +c / 4).toString(16)
    );
}

const env = {
    apiHost: "http://localhost:8080/JavaWebPv221"
};

const StateContext = React.createContext(null);

const initialState = {
    auth: {
        token: null,
        tmpId: null
    },
    page: "home",
    shop: {
        categories: []
    },
    cart: []
};

function reducer(state, action) {
    // console.log(action);
    switch( action.type ) {
        case 'auth' :
            window.sessionStorage.setItem( "token221", JSON.stringify( action.payload ) );
            window.localStorage.removeItem( "tmpId221" );
            return { ...state,
                auth: {
                    ...state.auth,
                    token: action.payload,
                    tmpId: null
                }
            };
        case 'auth-tmp' :
            return { ...state,
                auth: {
                    ...state.auth,
                    tmpId: action.payload
                }
            };
        case 'cart' :
            return { ...state,
                cart: action.payload
            };
        case 'navigate' :
            // console.log("navigate " + action.payload);
            window.location.hash = action.payload;
            return { ...state,
                page: action.payload
            };
        case 'setCategory' :
            return { ...state,
                shop: {
                    ...state.shop,
                    categories: action.payload
                }
            };
        default: throw Error('Unknown action.');
    }
}

function Spa() {
    const [state, dispatch] = React.useReducer( reducer, initialState );
    const [resource, setResource] = React.useState("");
    const exitClick = React.useCallback( () => {
        window.sessionStorage.removeItem( "token221" );
    });
    const resourceClick = React.useCallback( () => {
        const token = window.sessionStorage.getItem( "token221" );
        if( ! token ) {
            alert( "Запит ресурсу в неавторизованому режимі" );
            return;
        }
        fetch("spa", {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + JSON.parse(token).tokenId,
            }
        }).then(r => r.json()).then( j => {
            setResource( JSON.stringify(j) );
        });

    });
    const checkToken = React.useCallback( (forceAuth) => {
        let token = window.sessionStorage.getItem( "token221" );
        if(token) {
            token = JSON.parse(token);
            if( new Date(token.exp) < new Date() ) {
                exitClick();
            }
            else {
                if( forceAuth ) {
                    dispatch({ type: 'auth', payload: token });
                }
            }
        }
        else {
            let tmpId = window.localStorage.getItem( "tmpId221" );
            if( tmpId != null ) {
                state.auth.tmpId = tmpId;
                if( forceAuth ) {
                    dispatch({type: "auth-tmp", payload: tmpId});
                }
            }
        }
    });
    const hashChanged = React.useCallback( () => {
        const hash = window.location.hash;
        // console.log("hashChanged " + hash);
        if( hash.length > 1 ) {
            dispatch( { type: 'navigate', payload: hash.substring(1) } );
        }
    } );
    const request = React.useCallback( (url, params) => {
        if( url.startsWith( '/' ) ) {
            url = env.apiHost + url;
        }
        let bearer = null;
        if( state.auth.token != null ) {
            bearer = state.auth.token.tokenId;
        }
        else if( state.auth.tmpId != null ) {
            bearer = state.auth.tmpId;
        }

        if( bearer != null ) {
            if( typeof params == "undefined" ) {
                params = {};
            }
            if( typeof params.headers == "undefined" ) {
                params.headers = {
                    Authorization: 'Bearer ' + bearer
                };
            }
            else if( typeof params.headers.Authorization == "undefined" ) {
                params.headers.Authorization = 'Bearer ' + bearer;
            }
        }
        return new Promise((resolve, reject) => {
            fetch( url, params )
                .then(r => {
                    // перевірити на Content-Type (чи це json),
                    // а також на загальну помилку r.ok
                    return r.json();
                })
                .then(j => {
                    // перевірити на наявність j.status та j.data
                    if( j.status.isSuccessful ) {
                        resolve( j.data );
                    }
                    else {
                        reject( j.data );
                    }
                })
        });
    } );
    const loadCart = React.useCallback( () => {
        request( "/shop/cart" )
            .then( data => dispatch({type: 'cart', payload: data}) )
            .catch( console.error );
    } ) ;
    React.useEffect(() => {
        if(state.auth.token != null || state.auth.tmpId != null) {
            loadCart();
        }
        else {
            dispatch({type: 'cart', payload: []})
        }
    }, [state.auth]);
    React.useEffect(() => {
        hashChanged();
        checkToken(true);

        window.addEventListener('hashchange', hashChanged);
        const interval = setInterval(() => checkToken(false), 1000);

        if (state.shop.categories.length === 0) {
            fetch("shop/category")
                .then(r => r.json())
                .then(j => dispatch({type: 'setCategory', payload: j.data}));
        }

        return () => {
            clearInterval(interval);
            window.removeEventListener('hashchange', hashChanged);
        }
    }, []);
    const navigate = React.useCallback( (route) => {
        // console.log(route);
        // const action = { type: 'navigate', payload: route };
        dispatch( { type: 'navigate', payload: route } );
    });

    return <StateContext.Provider value={ {state, dispatch, request, loadCart} }>
        <h1>SPA</h1>
            <div>
                <button onClick={resourceClick} className="btn light-blue">Ресурс</button>
                <button onClick={exitClick} className="btn indigo lighten-4">Вихід</button>
                <p>{resource}</p>
                <b onClick={() => navigate('home')}>Home</b>&emsp;
                <b onClick={() => navigate('shop')}>Shop</b>&emsp;
                <b onClick={() => navigate('cart')}>Cart({state.cart.reduce( (cnt,c) => cnt + c.quantity, 0 )})</b>&emsp;
                { state.page === 'auth' && <AuthPage /> }
                { state.page === 'cart' && <Cart /> }
                { state.page === 'home' && <Home /> }
                { state.page === 'shop' && <Shop /> }
                { state.page.startsWith('category/') && <Category id={state.page.substring(9)} /> }
                { state.page.startsWith('product/') && <Product id={state.page.substring(8)} /> }
            </div>
    </StateContext.Provider>;
}

function AuthPage({backPage}) {
    const {state, dispatch, loadCart, request} = React.useContext(StateContext);
    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");
    const [error, setError] = React.useState(false);
    const loginChange = React.useCallback( (e) => setLogin( e.target.value ) );
    const passwordChange = React.useCallback( (e) => setPassword( e.target.value ) );
    const authClick = React.useCallback( () => {
        const credentials = btoa( login + ":" + password );
        let endpoint = `/auth`;
        if( state.auth.tmpId != null ) {
            endpoint += '?tmp-id=' + state.auth.tmpId;
            /*
            Реалізувати перенесення кошику з тимчасового ідентифікатора tmpId
            до авторизованого.
            Встановити Android Studio
            https://developer.android.com/studio
            Запустити, слідуючи інструкціям завантажити SDK та емулятор
            */
        }
        request( endpoint, {
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        })
        .then(data => {
            dispatch({type: 'auth', payload: data});
            setAuth(true);
            if( backPage ) {
                dispatch({type: 'navigate', payload: backPage});
            }
        })
        .catch(setError);
    });
    const [isAuth, setAuth] = React.useState(false);
    return <div>
        <b>Логін</b><input onChange={loginChange}/><br/>
        <b>Пароль</b><input type="password" onChange={passwordChange}/><br/>
        <button onClick={authClick}>Одержати токен</button>
        {error && <b>{error}</b>}
    </div>;
}

function Category({id}) {
    const {state, dispatch, loadCart, request} = React.useContext(StateContext);
    const [products, setProducts] = React.useState([]);
    const loadProducts = React.useCallback(() => {
        request(`/shop/product?categoryId=${id}`)
            .then(setProducts)
            .catch(err => {
                console.error(err);
                setProducts( null );
            });
    } );
    React.useEffect( () => {
        loadProducts();
    }, [id] );
    const addProduct = React.useCallback( (e) => {
        e.preventDefault();
        console.log(state.auth.token);
        const formData = new FormData(e.target);
        fetch(`${env.apiHost}/shop/product`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + state.auth.token.tokenId
            },
            body: formData
        }).then(r => r.json())
            .then(j => {
                if( j.status.isSuccessful ) {
                    loadProducts();
                    document.getElementById("add-product-form").reset();
                }
                else {
                    alert( j.data );
                }
            });
    });
    const addCart = React.useCallback( (id) => {
        // console.log( id );
        let userId;
        if( state.auth.token == null ) {
            if( state.auth.tmpId == null ) {
                state.auth.tmpId = uuidv4();
                window.localStorage.setItem( "tmpId221", state.auth.tmpId );
            }
            userId = state.auth.tmpId;
        }
        else {
            userId = state.auth.token.userId;
        }
        // TODO: check presence
        request("/shop/cart", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: userId,
                productId: id
            })
        }).then(() => loadCart())
            .catch(console.log);
    });
    return <div>
        {products && <div>
            Category: {id}<br/>
            <b onClick={() => dispatch({type: 'navigate', payload: 'home'})}>До Крамниці</b>
            <br/>
            {products.map(p => <div key={p.id}
                                    className="shop-product"
                                    onClick={() => dispatch({
                                        type: 'navigate',
                                        payload: 'product/' + (p.slug || p.id)
                                    })}>
                <b>{p.name}</b>
                <picture>
                    <img src={"file/" + p.imageUrl} alt="prod"/>
                </picture>
                <div className="row">
                    <div className="col s9">
                        <strong>{p.price} грн</strong>&emsp;
                        <small>{p.description}</small>
                    </div>
                    <div className="col s3">
                        <a className="btn-floating cart-fab waves-effect waves-light red"
                            onClick={(e) => {e.stopPropagation(); addCart(p.id)}}><i
                            className="material-icons">shopping_bag</i></a>
                    </div>
                </div>
            </div>)}
            <br/>
            {state.auth.token &&
                <form id="add-product-form" onSubmit={addProduct} encType="multipart/form-data">
                    <hr/>
                    <input name="product-name" placeholder="Назва"/>
                    <input name="product-slug" placeholder="Slug"/><br/>
                    <input name="product-price" type="number" step="0.01" placeholder="Ціна"/><br/>
                    Картинка: <input type="file" name="product-img"/><br/>
                    <textarea name="product-description" placeholder="Опис"></textarea><br/>
                    <input type="hidden" name="product-category-id" value={id} />
                    <button type="submit">Додати</button>
                </form>}
        </div>}
        {!products && <div>
            <h2>Група товарів {id} не існує</h2>
        </div>}
    </div>;
}

function Product({id}) {
    const [product, setProduct] = React.useState(null);
    React.useEffect( () => {
        request(`/shop/product?id=${id}`)
            .then( setProduct )
            .catch( err => {
                console.error( err );
                setProduct( null );
            });
    }, [id] );
    return <div>
        <h1>Сторінка товару</h1>

        {product && <div>
            <p>{product.name}</p>
        </div>}

        {!product && <div>
            <p>Шукаємо...</p>
        </div>}
        <hr/>
        <CategoriesList />
    </div>;
}

function Home() {
    const {state, dispatch} = React.useContext(StateContext);
    React.useEffect(() => {

    }, [] );
    return <React.Fragment>
        <h2>Home</h2>
        <b onClick={() => dispatch( { type: 'navigate', payload: 'shop' } )}>До Адмінки</b>
        <CategoriesList />
    </React.Fragment>;
}

function CategoriesList() {
    const {state, dispatch} = React.useContext(StateContext);
    return <div>
        {state.shop.categories.map(c =>
            <div key={c.id}
                 className="shop-category"
                 onClick={() => dispatch({type: 'navigate', payload: 'category/' + (c.slug || c.id)})}>
                <b>{c.name}</b>
                <picture>
                    <img src={"file/" + c.imageUrl} alt="grp"/>
                </picture>
                <p>{c.description}</p>
            </div>)}
    </div>;
}

function Shop() {
    const addCategory = React.useCallback((e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        fetch("shop/category", {
            method: 'POST',
            body: formData
        }).then(r => r.json()).then(console.log);
        // console.log(e);
    });

    return <React.Fragment>
        <h2>Shop</h2>
        <hr/>
        <form onSubmit={addCategory} encType="multipart/form-data">
            <input name="category-name" placeholder="Категорія"/>
            <input name="category-slug" placeholder="Slug"/><br/>
            Картинка: <input type="file" name="category-img"/><br/>
            <textarea name="category-description" placeholder="Опис"></textarea><br/>
            <button type="submit">Додати</button>
        </form>
    </React.Fragment>;
}

function Cart() {
    const {state, dispatch, loadCart, request} = React.useContext(StateContext);
    const changeQuantity = React.useCallback( (cartItem, action) => {
        switch (action) {
            case 'inc': updateCart(cartItem, 1);  break;
            case 'dec': updateCart(cartItem, -1);  break;
            case 'del': updateCart(cartItem, -cartItem.quantity);  break;
        }
    });
    const updateCart = React.useCallback( (cartItem, delta) => {
        if( Number(cartItem.quantity) + Number(delta) === 0
        && ! confirm( `Видалити з кошику '${cartItem.product.name}'?`) ) {
            return;
        }
        request("/shop/cart", {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                cartId: cartItem.cartId,
                productId: cartItem.productId,
                delta: delta
            })
        }).then(() => loadCart())
            .catch(console.log);
    });
    const buyClick = React.useCallback( () => {
        if( state.auth.token == null ) {
            // кошик в неавторизованому режимі
            if( confirm( "Ви купляєте в анонімному режимі. Історія " +
                "покупок збережена не буде, персональні знижки не враховуються." +
                "Бажаєте увійти до системи?" ) ) {
                dispatch({type: 'navigate', payload: 'auth'});
                return;
            }
        }
        if( confirm( `Підтверджуєте покупку на ${
            state.cart.reduce((prev,c)=>prev+c.quantity * c.product.price, 0)} грн?`)) {
            request("/shop/cart?cart-id=" + state.cart[0].cartId, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(() => loadCart())
                .catch(console.log);
        }
    });
    return <div>
        <h1>Кошик</h1>
        {state.cart.map(c => <div key={c.productId}>
            <img src={"file/" + c.product.imageUrl} alt="prod" width="55"/>
            {c.product.name} {c.product.price}
            <span>
                <button onClick={() => changeQuantity(c, 'dec')}>-</button>
                {c.quantity}
                <button onClick={() => changeQuantity(c, 'inc')}>+</button>
                &emsp;
                <button onClick={() => changeQuantity(c, 'del')}>x</button>
            </span>
        </div>)}
        <b>Разом: {state.cart.reduce((prev,c)=>prev+c.quantity * c.product.price, 0)}</b>
        <button onClick={buyClick}>Придбати</button>
    </div>
}

ReactDOM
    .createRoot(document.getElementById("spa-container"))
    .render(<Spa />);
/*
Д.З. Зробити дизайн сторінки "Кошик"
- табличне представлення даних (або таблицею, або однаковими блоками)
- додати загальну вартість позицію (ціна х кількість)
- загальна кількість товарів та загальна сума під таблицею (за умови,
   що є наповнення, інакше - виводити "кошик порожній")
- на загальному віджеті окрім кількості товарів у кошику виводити його вартість
 */