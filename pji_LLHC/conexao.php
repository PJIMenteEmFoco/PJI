<?php
    include_once("servidorConn.php");

    $servidor = "localhost";
    $usuario = "root";
    $senhaBanco = "";
    $banco = "teste_banco";

    $conn = abrirConexao($servidor, $usuario, $senha, $banco);
    if ($conn->connect_error) {
        echo "conectado!";
    }
?>