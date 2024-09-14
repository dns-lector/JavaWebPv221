document.addEventListener("submit", e => {
    const form = e.target;
    if(form.id === "signup-form") {
        e.preventDefault();
        const formData = new FormData(form);
        fetch(form.action, {
            method: "POST",
            body: formData
        }).then(r => r.json())
            .then(j => {
            console.log(j);
        });
    }
});