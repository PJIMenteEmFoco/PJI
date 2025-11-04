<?php
    include_once ('conexao.php' );
    session_start();
    $nome = $_POST['txtName'];
    $email = $_POST['txtEmail'];
    $senha = $_POST['txtSenha'];

    $stmt = $conn->prepare("INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)");

    $stmt->bind_param("sss", $nome, $email, $senha);
    if ($stmt->execute()) {
        echo "Novo Registro inserido com sucesso";
        header('Location:login.php');
    } else {
        echo "Erro ao inserir registro: ". $stmt->error;
    }

    $stmt->close();
    $conn->close();
?>