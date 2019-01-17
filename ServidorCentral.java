import java.rmi.Naming;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Scanner;
import java.util.Properties;
import java.text.SimpleDateFormat;

public class ServidorCentral
{
    public static void startServer()
    {
        try
        {
            String path = System.getProperty("user.home") + "/ProjetoSD/BancoCentral";

            File configs = new File(path + "/configs");            
            File arqLog = new File(path + "/log.txt");
            File bancos = new File(path + "/bancos");

            log("INICIANDO SERVER");
            log("LENDO CONFIGURACOES");

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
            
            String ip = props.getProperty("ip");
            
            log("ARQUIVO DE CONFIGURACAO OK");

            FileInputStream in = new FileInputStream(bancos);
            Properties prop = new Properties();
            prop.load(in);
            in.close();

            log("LISTA DE BANCOS OK");

            BancoCentral banco = new BancoCentral(ip, prop, bancos, arqLog);

            Naming.rebind("rmi://" + banco.getIp() + "/BancoCentral", banco);

            System.out.println();
            log("IP: " + ip);
            System.out.println();
            log("SERVER ONLINE");
            System.out.println();
        }
        catch(FileNotFoundException e)
        {
            log("ERRO AO ACESSAR ARQUIVOS DE CONFIGURACOES: " + e.getMessage());
            System.exit(0);
        }
        catch(Exception e)
        {
            log("ERRO AO INICIAR O SERVIDOR: " + e.getMessage());
            System.exit(0);
        }
    }

    public static void configurar()
    {
        Scanner scan = new Scanner(System.in);
        
        String path = System.getProperty("user.home") + "/ProjetoSD/BancoCentral/";
        
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
                File bancos = new File(path + "bancos");
                
                try
                {
                    System.out.println("Criando log");
                    arqLog.createNewFile();
                    System.out.println("Criando registro de bancos");
                    bancos.createNewFile();
                    System.out.println("Criando arquivo de configuracões");
                    configs.createNewFile();
                    
                    Properties props = new Properties();
                    
                    System.out.println("Digite o ip do banco:");
                    props.setProperty("ip", scan.nextLine());
                    
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
            File bancos = new File(path + "bancos");
            
            try
            {
                if(!arqLog.exists())
                {
                    System.out.println("Log faltando. Criando log");
                    arqLog.createNewFile();
                }
                
                if(!bancos.exists())
                {
                    System.out.println("Registro de bancos faltando. Criando registro");
                    bancos.createNewFile();
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

                System.out.println("Digite o ip do banco:");
                props.setProperty("ip", scan.nextLine());

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
    
    public static void log(String mensagem)
    {
        Calendar data = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");

        File arquivoLog = new File(System.getProperty("user.dir") + "/BancoCentral/log.txt");

        try
        {
            FileWriter fw = new FileWriter(arquivoLog, true);
            PrintWriter pw = new PrintWriter(fw, true);

            pw.println("(" + sdf.format(data.getTime()) + ") " + mensagem);
        }
        catch(Exception e)
        {
            System.out.println("ERRO AO ESCREVER LOG");
        }

        System.out.println("(" + sdf.format(data.getTime()) + ") " + mensagem);
    }

    public static void main(String args[])
    {
        Scanner scan = new Scanner(System.in);
        
        System.out.println("========SERVIDOR BANCO=======");
        
        String path = System.getProperty("user.home") + "/ProjetoSD/BancoCentral/";
        
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