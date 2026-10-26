/* Utilitários compartilhados pelas telas de login e cadastro do Mente em Foco. */
(function() {
    'use strict';

    function togglePassword(fieldId) {
        const field = document.getElementById(fieldId);
		const btn = document.getElementsByClassName('password-toggle')
        if (!field) return;
        field.type = field.type === 'password' ? 'text' : 'password';
    }
    window.togglePassword = togglePassword;

    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toast-message');
    let toastTimeout;

    function hideToast() {
        if (toast) toast.classList.remove('show');
        if (toastTimeout) window.clearTimeout(toastTimeout);
    }
    window.hideToast = hideToast;

    function showToast(message, redirectToLogin) {
        if (!toast || !toastMessage) return;
        toastMessage.textContent = message;
        toast.classList.add('show');
        if (toastTimeout) window.clearTimeout(toastTimeout);
        toastTimeout = window.setTimeout(function() {
            hideToast();
            if (redirectToLogin) window.location.assign('/login');
        }, 3000);
    }
    window.showToast = showToast;

    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.addEventListener('input', function(event) {
            let value = event.target.value.replace(/\D/g, '').slice(0, 11);
            if (value.length > 10) {
                value = value.replace(/^(\d{2})(\d{5})(\d{4})$/, '($1) $2-$3');
            } else if (value.length > 6) {
                value = value.replace(/^(\d{2})(\d{4})(\d{0,4})$/, '($1) $2-$3');
            } else if (value.length > 2) {
                value = value.replace(/^(\d{2})(\d{0,5})$/, '($1) $2');
            } else if (value.length > 0) {
                value = '(' + value;
            }
            event.target.value = value;
        });
    }

    const birthDateInput = document.getElementById('birthDate');
    if (birthDateInput) {
        birthDateInput.addEventListener('input', function(event) {
            let value = event.target.value.replace(/\D/g, '').slice(0, 8);
            if (value.length > 4) {
                value = value.replace(/^(\d{2})(\d{2})(\d{0,4})$/, '$1/$2/$3');
            } else if (value.length > 2) {
                value = value.replace(/^(\d{2})(\d{0,2})$/, '$1/$2');
            }
            event.target.value = value;
        });
    }

    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const ruleUppercase = document.getElementById('rule-uppercase');
    const ruleLowercase = document.getElementById('rule-lowercase');
    const ruleNumber = document.getElementById('rule-number');
    const ruleSpecial = document.getElementById('rule-special');
    const ruleLength = document.getElementById('rule-length');

    function updatePasswordRules() {
        if (!passwordInput) return;
        const value = passwordInput.value;
        if (ruleUppercase) ruleUppercase.classList.toggle('valid', /[A-Z]/.test(value));
        if (ruleLowercase) ruleLowercase.classList.toggle('valid', /[a-z]/.test(value));
        if (ruleNumber) ruleNumber.classList.toggle('valid', /\d/.test(value));
        if (ruleSpecial) ruleSpecial.classList.toggle('valid', /[@$!%*?&#^()_\-+=\[\]{}|\\;:'",.<>\/`]/.test(value));
        if (ruleLength) ruleLength.classList.toggle('valid', value.length >= 8);
    }
    if (passwordInput) passwordInput.addEventListener('input', updatePasswordRules);

    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', function(event) {
            const fullName = document.getElementById('fullName');
            const email = document.getElementById('email');
            const password = passwordInput;
            const confirmation = confirmPasswordInput;
            const phone = phoneInput;
            const birthDate = birthDateInput;
            const profile = registerForm.querySelector('input[name="perfil"]:checked');

            const values = [fullName, email, password, confirmation, phone, birthDate];
            if (values.some(function(field) { return !field || !field.value.trim(); }) || !profile) {
                event.preventDefault();
                showToast('Preencha todos os campos antes de continuar.', false);
                return;
            }

            const phoneDigits = phone.value.replace(/\D/g, '');
            if (phoneDigits.length !== 10 && phoneDigits.length !== 11) {
                event.preventDefault();
                showToast('Informe um telefone válido com DDD.', false);
                phone.focus();
                return;
            }

            if (!isValidDate(birthDate.value.trim())) {
                event.preventDefault();
                showToast('Informe uma data de nascimento válida no formato dd/mm/aaaa.', false);
                birthDate.focus();
                return;
            }

            if (password.value.length < 8 || !/[A-Z]/.test(password.value)
                || !/[a-z]/.test(password.value) || !/\d/.test(password.value)
                || !/[@$!%*?&#^()_\-+=\[\]{}|\\;:'",.<>\/`]/.test(password.value)
                || !/^[A-Za-z\d@$!%*?&#^()_\-+=\[\]{}|\\;:'",.<>\/`]{8,}$/.test(password.value)) {
                event.preventDefault();
                showToast('A senha deve ter 8 caracteres, incluindo maiúscula, minúscula, número e caractere especial.', false);
                password.focus();
                return;
            }

            if (password.value !== confirmation.value) {
                event.preventDefault();
                showToast('As senhas não coincidem. Confira os dois campos.', false);
                confirmation.focus();
                return;
            }
            // Form válido: não cancelar o envio. O POST é processado pelo Spring Boot.
        });
    }

    function isValidDate(value) {
        const match = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);
        if (!match) return false;
        const day = Number(match[1]);
        const month = Number(match[2]);
        const year = Number(match[3]);
        const date = new Date(year, month - 1, day);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return date.getFullYear() === year
            && date.getMonth() === month - 1
            && date.getDate() === day
            && date <= today;
    }

    // Mostra erros retornados pelo backend e reproduz o toast/redirect de sucesso do PJI.
    if (toast && toast.classList.contains('show')) {
        const isSuccess = toast.dataset.sucesso === 'true';
        const message = toastMessage ? toastMessage.textContent : '';
        showToast(message, isSuccess);
    }
})();
