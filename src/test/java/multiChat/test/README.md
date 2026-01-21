# Explicacion de estructura


    /common
        /protocolo (Constantes)
        /NetUtils
    /cliente
        /ChatClienteMain
        /ChatClienteGUI (El hilo para teclado y botones)
        /ChatCliente
        /ServerListener  (Soluciona el Thread EDT , pendiente del buzon)
        /ServerLogger   (log con fecha y hora)
    /server
        /ChatServerMain
        /ChatServer
        /ClienteHandler
        /ServerLogger   (log con fecha y hora)

- Separacion de conceptos -> red-GUI-logs
- Cada fichero tiene un objetivo.

## Tareas del Servidor
    -   Escuchara en el puerto:
    -   Aceptara o Rechazara un cliente cuando se intente conectar
    -   Para cada cliente crea un Thread
    -   Tendra una lista/mapa de clientes conectados(usuarios)
        asi evitamos Clientes repetidos, reenviar mensajes
    -   Cada mensaje recibido se envia a ChatServer_log.txt con fecha hora y nombre

## Tareas del Cliente
    -   Tiene una interfaz (GUI)
    -   Se le pide
        - host
        - nombre de usuario
    -   Se conecta al servidor por Socket
    -   Entra al chat puede leer (en tiempo real) y enviar mensajes 

## Corazon del Chat
    . Cliente envia
        -"hola"
        - El cliente manda -> MSG|hola
    . Servidor recibe y emite
        - El hilo del cliente(ClienteHandler) lee el MSG|hola
        - El servidor hace broadcast
            -Recorre la lista de clientes conectados a cada uno le envia el MSG|hola
        - El servidor escribe en ChatServer_log.txt
