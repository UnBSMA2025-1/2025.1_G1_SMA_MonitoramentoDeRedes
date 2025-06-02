    const loginForm = document.getElementById("login-form");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const submitButton = document.getElementById("submit-button");
    const responseMessageDiv = document.getElementById("response-message");
    const originalButtonText = submitButton.textContent;

    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        if (!username || !password) {
            setResponseMessage("Por favor, preencha usuário e senha.", "error");
            return;
        }

        setLoadingState(true);

        try {
            const response = await fetch("/", { // Seu endpoint de login
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({username, password})
            });

            let responseBodyText = "";
            try {
                responseBodyText = await response.text();
            } catch (textError) {
                console.warn("Não foi possível ler o corpo da resposta como texto.", textError);
            }

            if (response.ok) { // Se o login for bem-sucedido (status 2xx)
                let data;
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.includes("application/json")) {
                    try {
                        data = JSON.parse(responseBodyText);
                    } catch (jsonError) {
                        console.error("Erro ao parsear JSON da resposta:", jsonError);
                        data = { message: "Login bem-sucedido, mas resposta inesperada." };
                        setResponseMessage(data.message, "success");
                    }
                } else {
                    data = {message: responseBodyText || "Login bem-sucedido!"};
                }

                setResponseMessage(data.message || "Login realizado com sucesso!", "success");
                loginForm.reset();

                setTimeout(() => {
                    window.location.href = '/dashboard.html'; // OU data.redirectUrl se o servidor enviar
                }, 1500);

            } else { // Se o login falhar
                let errorMessage = `Erro: ${response.status} ${response.statusText}`;
                if (responseBodyText) {
                    try {
                        const errorData = JSON.parse(responseBodyText);
                        errorMessage = errorData.message || errorData.error || responseBodyText;
                    } catch (e) {
                        errorMessage = responseBodyText;
                    }
                }
                setResponseMessage(errorMessage, "error");
                setLoadingState(false); // Reabilita o botão em caso de erro
            }

        } catch (networkError) { // Erro de rede (servidor fora, sem conexão)
            console.error("Erro na requisição de login (rede):", networkError);
            setResponseMessage("Não foi possível conectar ao servidor. Verifique sua conexão.", "error");
            setLoadingState(false);
        }
    });

    function setLoadingState(isLoading) {
        if (isLoading) {
            submitButton.disabled = true;
            submitButton.innerHTML = `${originalButtonText} <span class="spinner"></span>`;
        } else {
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
        }
    }

    function setResponseMessage(message, type = "info") {
        responseMessageDiv.textContent = message;
        responseMessageDiv.className = 'response'; 
        if (type === 'success' || type === 'error') {
            responseMessageDiv.classList.add(type);
        }
}