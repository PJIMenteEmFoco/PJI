<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro Mente em Foco</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="caixa-form">
        <h2 class="subtitulo">Crie sua Conta</h2>
        <p class="instrucao">Junte-se ao Mente em Foco para começar</p>

        <form action="insercao.php" method="POST" class="formulario-cadastro">
            
            <label for="tipo-Usuario" class="campo">Tipo de Usuario</label>
            <select id="tipo-Usuario" class="campo-selecionar">
                <option>Selecione...</option>
                <option name="Professor">Professor</option>
                <option name="Aluno">Aluno</option>
                <option name="Psicopedagogo">Psicopedagogo</option>
            </select>

            <label for="nome" class="campo">Nome: </label>
            <input type="text" name="txtName" class="campo-input" required>
            
            <label for="email" class="campo">E-mail: </label>
            <input type="email" name="txtEmail" class="campo-input"required>
            
            <label for="senha" class="campo">Senha: </label>
            <input type="password" name="txtSenha" class="campo-input"required>
            
            <input type="submit" value="Cadastrar" class="botao">
            
            <p class="link-secundario">Já tem cadastro? <a href="login.php">Faça o Login!</a></p>
        </form>
    </div>
</body>
</html>