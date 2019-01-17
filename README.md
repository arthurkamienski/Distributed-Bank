# Distributed-Bank
This is a distributed bank application written in Java using Remote Method Invocation (RMI) with a GUI. It was developed as a final project for the Distributed Systems course in UFABC (Universidade Federal do ABC). 

This application has three different components: a client, a bank, and a central bank (which acts as a binder among banks).

To properly run this application, follow the steps:

1 - Compile all of the files

2 - Compile "Conta", "Banco" and "BancoCentral" using the RMI compiler (RMIC command)

3 - Execute RMIRegistry inside the directory where the files are

4 - Execute the central bank via ServidorCentral.java (not mandatory but necessary to make transactions between banks)

5 - Configure the central bank (this creates a directory called "ProjetoSD/BancoCentral" in the home directory)

6 - Execute the bank using "Servidor.java" (mandatory)

7 - Configure the bank (also creates a directory in "ProjesoSD")

8 - Execute the client via ClientGUI.java

9 - Configure the clietn (creates a diretory in "ProjetoSD")
