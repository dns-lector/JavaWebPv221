const env = {
    apiHost: "http://localhost:8080/JavaWebPv221"
};

function request(url, params) {
    if( url.startsWith( '/' ) ) {
        url = env.apiHost + url;
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
}

const initialState = {
    auth: {
        token: null
    },
    page: "home",
    shop: {
        categories: [ ]
    }
};

function reducer(state, action) {
    // console.log(action);
    switch( action.type ) {
        case 'auth' :
            return { ...state,
                auth: {
                    ...state.auth,
                    token: action.payload
                }
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

const StateContext = React.createContext(null);

function Spa() {
    const [state, dispatch] = React.useReducer( reducer, initialState );
    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");
    const [error, setError] = React.useState(false);
    const [isAuth, setAuth] = React.useState(false);
    const [resource, setResource] = React.useState("");
    const loginChange = React.useCallback( (e) => setLogin( e.target.value ) );
    const passwordChange = React.useCallback( (e) => setPassword( e.target.value ) );
    const authClick = React.useCallback( () => {
        const credentials = btoa( login + ":" + password );
        fetch(`${env.apiHost}/auth`, {
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        }).then(r => r.json()).then( j => {
            if( j.status.isSuccessful ) {
                window.sessionStorage.setItem( "token221", JSON.stringify( j.data ) );
                setAuth(true);
            }
            else {
                setError( j.data );
            }
        });
        console.log(credentials);
    } );
    const exitClick = React.useCallback( () => {
        window.sessionStorage.removeItem( "token221" );
        setAuth(false);
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
    const checkToken = React.useCallback( () => {
        let token = window.sessionStorage.getItem( "token221" );
        // console.log( token, !!token, isAuth );
        if(token) {
            token = JSON.parse(token);
            if( new Date(token.exp) < new Date() ) {
                exitClick();
            }
            else {
                if(!isAuth) {
                    setAuth(true);
                    dispatch({ type: 'auth', payload: token });
                }
            }
        }
        else {
            setAuth(false);
        }
    });
    const hashChanged = React.useCallback( () => {
        const hash = window.location.hash;
        // console.log("hashChanged " + hash);
        if( hash.length > 1 ) {
            dispatch( { type: 'navigate', payload: hash.substring(1) } );
        }
    } );
    React.useEffect(() => {
        hashChanged();
        checkToken();
        window.addEventListener('hashchange', hashChanged);
        const interval = setInterval(checkToken, 1000);

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

    return <StateContext.Provider value={ {state, dispatch} }>
        <h1>SPA</h1>
        { !isAuth &&
            <div>
                <b>Логін</b><input onChange={loginChange} /><br/>
                <b>Пароль</b><input type="password"  onChange={passwordChange} /><br/>
                <button onClick={authClick}>Одержати токен</button>
                {error && <b>{error}</b>}
            </div>
        }{ isAuth &&
            <div>
                <button onClick={resourceClick} className="btn light-blue">Ресурс</button>
                <button onClick={exitClick} className="btn indigo lighten-4">Вихід</button>
                <p>{resource}</p>
                <b onClick={() => navigate('home')}>Home</b>
                <b onClick={() => navigate('shop')}>Shop</b>
                { state.page === 'home' && <Home /> }
                { state.page === 'shop' && <Shop /> }
                { state.page.startsWith('category/') && <Category id={state.page.substring(9)} /> }
                { state.page.startsWith('product/') && <Product id={state.page.substring(8)} /> }
            </div>
        }
    </StateContext.Provider>;
}

function Category({id}) {
    const {state, dispatch} = React.useContext(StateContext);
    const [products, setProducts] = React.useState([]);
    const loadProducts = React.useCallback( () => {
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
    return <div>
        {products && <div>
            Category: {id}<br/>
            <b onClick={() => dispatch({type: 'navigate', payload: 'home'})}>До Крамниці</b>
            <br/>
            {products.map(p => <div key={p.id}
                                    className="shop-product"
                                    onClick={() => dispatch({type: 'navigate', payload: 'product/' + (p.slug || p.id)})}>
                <b>{p.name}</b>
                <picture>
                    <img src={"file/" + p.imageUrl} alt="prod"/>
                </picture>
                <p><strong>{p.price}</strong> <small>{p.description}</small></p>
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


ReactDOM
    .createRoot(document.getElementById("spa-container"))
    .render(<Spa />);
/*
Д.З. Додати до компонента CategoriesList параметр
який відповідатиме за відображення:
<CategoriesList mode="table" /> - як на головній сторінці - "таблицею": великими блоками
<CategoriesList mode="ribbon" /> - "стрічкою" - для додаткового відображення на
 інших сторінках
 */