<?php
    function abrirConexao($servidor, $usuario, $senha, $banco) {
        return new mysqli($servidor, $usuario, $senha, $banco);
    }
?>