const initialState = {
    page: "home"
};

function reducer(state, action) {
    // console.log(action);
    switch( action.type ) {
        case 'navigate' :
            return { ...state,
                page: action.payload
            };
        default: throw Error('Unknown action.');
    }
}

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
        fetch("auth", {
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        }).then(r => r.json()).then( j => {
            if( j.status === "Ok") {
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
                if(!isAuth) setAuth(true);
            }
        }
        else {
            setAuth(false);
        }
    });
    React.useEffect(() => {
        checkToken();
        const interval = setInterval(checkToken, 1000);

        return () => clearInterval(interval);
    }, []);

    const navigate = React.useCallback( (route) => {
        // console.log(route);
        // const action = { type: 'navigate', payload: route };
        dispatch( { type: 'navigate', payload: route } );
    });

    return <React.Fragment>
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
                { state.page === 'home' && <h2>Home</h2> }
                { state.page === 'shop' && <Shop /> }
            </div>
        }
    </React.Fragment>;
}

function Shop() {
    const addCategory = React.useCallback( (e) => {
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
            <input name="category-name" placeholder="Категорія"/><br/>
            Картинка: <input type="file" name="category-img"/><br/>
            <textarea name="category-description" placeholder="Опис"></textarea><br/>
            <button type="submit">Додати</button>
        </form>
    </React.Fragment>;
}

ReactDOM
    .createRoot(document.getElementById("spa-container"))
    .render(<Spa />);