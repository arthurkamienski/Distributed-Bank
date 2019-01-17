Projeto de Sistemas Distribuídos

Projeto de banco distribuído: cliente, banco e banco central (que age como binder ligando os bancos) usando RMI da JVM.

Para executar:

1 - Compile todos os arquivos usando a JVM
2 - Compile "Conta", "Banco" e "BancoCentral" usando o compilador RMI (comando RMIC)
3 - Execute o RMIRegistry na pasta contendo todos os arquivos
4 - Execute o Banco Central por ServidorCentral.java (não é obrigatório, mas necessário pra realizar transações entre bancos)
5 - Configure o Banco Central (cria uma pasta (ProjetoSD/BancoCentral/) na pasta "home" do pc)
6 - Execute o banco por Servidor.java (obrigatório)
7 - Configure o banco (também cria pasta (ProjetoSD/Banco))
9 - Execute o client por ClientGUI.java
10 - Configure o client (também cria pasta (ProjetoSD/Cliente))

***Pra hostear os servers no próprio pc, use o IP "127.0.0.1", "localhost" ou "192.168.?.?" (não lembro)
***Para hostear os servers em outro pc, configure o arquivo "hosts" do pc em que está o server para que o nome dele seja associado ao IP
***Talvez precise configurar o modem