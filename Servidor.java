import java.rmi.Naming;
import java.util.Scanner;
import java.util.Calendar;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class Servidor
{
    public static void startServer()
    {
        try
        {   
            String path = System.getProperty("user.home") + "/ProjetoSD/Banco2/";
            File arqLog = new File(path + "log.txt");
            File configs = new File(path + "configs");
            File transfs = new File(path + "Transfs");
            File contas = new File(path + "Contas");

            System.out.println("INICIANDO SERVER");
            System.out.println("LENDO CONFIGURACOES");
            
            Properties props = new Properties();
            
            try
            {
                FileInputStream in = new FileInputStream(configs);
                
                props.load(in);
            }
            catch(FileNotFoundException e)
            {
                throw new FileNotFoundException("ARQUIVO DE CONFIGURACAO FALTANDO.");
            }

            System.out.println("ARQUIVO DE CONFIGURACAO OK");
            
            String nome = props.getProperty("nomeBanco");
            String ipBancoCentral = props.getProperty("ipBancoCentral");
            String ip = props.getProperty("ipBanco");

            if(!contas.isDirectory())
                throw new FileNotFoundException("DIRETORIO CONTAS FALTANDO");

            System.out.println("DIRETORIO DE CONTAS OK");

            try
            {
                IBancoCentral bancoCentral = (IBancoCentral) Naming.lookup("rmi://" + ipBancoCentral + "/BancoCentral");
                bancoCentral.novoBanco(nome);                
                System.out.println("CONEXAO COM BANCO CENTRAL OK");
            }
            catch(Exception e)
            {
                System.out.println("ERRO AO CONTACTAR BANCO CENTRAL");
            }

            String servico = "rmi://" + ip + "/" + nome;

            Banco banco = new Banco(nome, ipBancoCentral, ip, servico, contas, arqLog, transfs);

            Naming.rebind(servico, banco);

            System.out.println();
            System.out.println("=====SERVER ONLINE=====");
            System.out.println("NOME BANCO: " + nome);
            System.out.println("IP BANCO: " + ip);
            System.out.println("IP BANCO CENTRAL: " + ipBancoCentral);
            System.out.println("DIRETORIO CONTAS: " + contas);
            System.out.println();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("ERRO AO ACESSAR ARQUIVOS DE CONFIGURACOES: " + e.getMessage());
            System.exit(0);
        }
        catch(Exception e)
        {
            System.out.println("ERRO AO INICIAR O SERVIDOR: " + e.getMessage());
            System.exit(0);
        }
    }
    
    public static void configurar()
    {
        Scanner scan = new Scanner(System.in);
        
        String path = System.getProperty("user.home") + "/ProjetoSD/Banco2/";
        
        File dir = new File(path);
        
        if(!dir.exists())
        {
            System.out.println("Deseja instalar o servidor? (S/N)");
            String resposta = scan.nextLine();
        
            if(resposta.equals("S"))
            {
                dir.mkdirs();
                
                File arqLog = new File(path + "log.txt");
                File configs = new File(path + "configs");
                File transfs = new File(path + "Transfs");
                File naodef = new File(path + "Contas/naodef");
                
                try
                {
                    System.out.println("Criando log");
                    arqLog.createNewFile();
                    System.out.println("Criando diretório de contas");
                    naodef.mkdirs();
                    System.out.println("Criando diretório de transferências");
                    transfs.mkdirs();
                    System.out.println("Criando arquivo de configuracões");
                    configs.createNewFile();
                    
                    Properties props = new Properties();
                    
                    System.out.println("Digite o nome do banco:");
                    props.setProperty("nomeBanco", scan.nextLine());
                    
                    System.out.println("Digite o ip do banco:");
                    props.setProperty("ipBanco", scan.nextLine());
                    
                    System.out.println("Digite o ip do banco central:");
                    props.setProperty("ipBancoCentral", scan.nextLine());
                    
                    FileOutputStream out = new FileOutputStream(configs);
                    props.store(out, null);
                    out.close();
                            
                    System.out.println("Instalação Completa. Reinicie o server.");
                }
                catch(Exception e)
                {
                    System.out.println("Erro na instalação.");
                }
            }
                
            else
                configurar();
        }
        else
        {
            File arqLog = new File(path + "log.txt");
            File configs = new File(path + "configs");
            File transfs = new File(path + "Transfs");
            File naodef = new File(path + "Contas/naodef");
            
            try
            {
                if(!arqLog.exists())
                {
                    System.out.println("Log faltando. Criando log");
                    arqLog.createNewFile();
                }
                
                if(!naodef.exists())
                {
                    System.out.println("Diretório de contas faltando. Criando diretório");
                    naodef.mkdirs();
                }
                
                if(!transfs.getParentFile().exists())
                {
                    System.out.println("Diretório de transferências faltando. Criando diretório");
                    transfs.mkdirs();
                }
                
                if(!configs.exists())
                {
                    System.out.println("Arquivo de configurações faltando. Criando arquivo");
                    configs.createNewFile();
                }
                
                if(!configs.exists())
                {
                    System.out.println("Arquivo de configurações faltando. Criando arquivo");
                    configs.createNewFile();
                }
                    
                if(!configs.exists())
                {
                    System.out.println("Arquivo de configurações faltando. Criando arquivo");
                    configs.createNewFile();
                }
                
                Properties props = new Properties();

                System.out.println("Digite o nome do banco:");
                props.setProperty("nomeBanco", scan.nextLine());

                System.out.println("Digite o ip do banco:");
                props.setProperty("ipBanco", scan.nextLine());

                System.out.println("Digite o ip do banco central:");
                props.setProperty("ipBancoCentral", scan.nextLine());

                FileOutputStream out = new FileOutputStream(configs);
                props.store(out, null);
                out.close();

                System.out.println("Configuração Completa. Reinicie o server.");
            }
            catch(Exception e)
            {
                System.out.println("Erro na configuração.");
            }
        }
    }

    public static void main(String args[])
    {
        Scanner scan = new Scanner(System.in);
        
        System.out.println("========SERVIDOR BANCO=======");
        
        String path = System.getProperty("user.home") + "/ProjetoSD/Banco2/";
        
        File dir = new File(path);
        
        if(dir.exists())
        {
            System.out.println("0 - Configurar");
            System.out.println("1 - Iniciar Servidor");

            String resposta = scan.nextLine();
            
            System.out.println("");

            if(resposta.equals("1"))
                startServer();
            else
                configurar();
        }
        else
            configurar();
    }
}
