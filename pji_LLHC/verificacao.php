<?php
    session_start();

    include_once('conexao.php');

    function verificar(string $email, string $senha, mysqli $conn):bool {
        $sql = "SELECT senha FROM usuarios WHERE email = ?";
        $stmt = $conn->prepare($sql);

        $stmt->bind_param("s", $email);
        $stmt->execute();
        $resultado = $stmt->get_result();

        if($resultado->num_rows === 1) {
            $usuario = $resultado->fetch_assoc();
            $senha_do_banco = $usuario['senha'];

        if($senha === $senha_do_banco) {
            return true;
            }
        }

        return false;
    }

    $login = $_POST['txtEmailLogin'];
    $senhaLogin = $_POST['txtSenhaLogin'];

    if(verificar($login, $senhaLogin, $conn)) {
        $_SESSION['login'] = $login;
        header('Location:dashboard.php');
    } else {
        setcookie("error","erro",time()+2);
        unset($_SESSION['login']);
        header('Location:login.php');
    }
?>