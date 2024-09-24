function Spa() {
    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");
    const loginChange = React.useCallback( (e) => setLogin( e.target.value ) );
    const passwordChange = React.useCallback( (e) => setPassword( e.target.value ) );
    const authClick = React.useCallback( () => {
        const credentials = btoa( login + ":" + password );
        fetch("auth", {
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        }).then(r => r.json()).then( console.log );
        console.log(credentials);
    } );

    return <React.Fragment>
        <h1>SPA</h1>
        <div>
            <b>Логін</b><input onChange={loginChange} /><br/>
            <b>Пароль</b><input type="password"  onChange={passwordChange} /><br/>
            <button onClick={authClick}>Одержати токен</button>
        </div>
    </React.Fragment>;
}

ReactDOM
    .createRoot(document.getElementById("spa-container"))
    .render(<Spa />);