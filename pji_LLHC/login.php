<?php
    session_start();
?>
<!DOCTYPE html>
<html lang="pt-br">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Mente em Foco</title>
    <link rel="stylesheet" href="style.css">
</head>

<body>
    <div class="caixa-form">
        <h2 class="subtitulo">Bem-vindo ao Mente em Foco</h2>
        <p class="instrucao">Entre para acessar sua conta</p>

        <form action="verificacao.php" method="POST" class="formulario-login">
            
            <label for="tipo-Usuario" class="campo">Tipo de Usuario</label>
            <select id="tipo-Usuario" class="campo-selecionar">
                <option>Selecione...</option>
                <option name="Professor">Professor</option>
                <option name="Aluno">Aluno</option>
                <option name="Psicopedagogo">Psicopedagogo</option>
            </select>

            <label for="email" class="campo">E-mail: </label>
            <input type="email" name="txtEmailLogin" class="campo-input" required>

            <label for="senha" class="campo">Senha: </label>
            <input type="password" name="txtSenhaLogin" class="campo-input" required>
            
            <input type="submit" value="Entrar" class="botao">
            
            <?php
            if (isset($_COOKIE["error"])) {
                echo ("<div class='alert-danger' role='alert'> Usuário inválido </div> ");
            }
            ?>
            
            <p class="link-secundario">Não tem cadastro? <a href="cadastro.php">Faça o Cadastro!</a></p>
        </form>
    </div>
</body>

</html>