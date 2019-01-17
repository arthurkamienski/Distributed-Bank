import java.rmi.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

public class ClienteGUI
{
    public static void verificaCpf(String cpf) throws Exception
    {
        boolean ok = true;
        
        int[] vetCpf = new int[11];
        int k = 0;
        
        if(cpf.length() == 11)
        {
            for(int i=0; i<9; i++)
            {
                vetCpf[i] = java.lang.Character.getNumericValue(cpf.charAt(i));

                k += vetCpf[i] * (10-i);
            }
            
            vetCpf[9] = java.lang.Character.getNumericValue(cpf.charAt(9));
            vetCpf[10] = java.lang.Character.getNumericValue(cpf.charAt(10));

            if(vetCpf[9] == (k*10)%11)
            {
                k = 0;

                for(int i=0; i<10; i++)
                    k += vetCpf[i] * (11-i);

                if(vetCpf[10] != (k*10)%11)
                    ok = false;
            }
            else
                ok = false;
        }
        else
            ok = false;
        
        if(!ok)
            throw new Exception("CPF inválido.");
    }
    
    public static void configura(Properties prop) throws Exception
    {
        String path = System.getProperty("user.home") + "/ProjetoSD/Cliente";
        
        File dirConf = new File(path);
        
        if(!dirConf.exists())
            dirConf.mkdirs();
        
        File arq = new File(path + "/configs");
        
        if(!arq.exists())
            arq.createNewFile();
        
        try
        {
            FileOutputStream out = new FileOutputStream(arq);
            prop.store(out, null);
            out.close();
        }
        catch(Exception e)
        {
            throw new Exception("Erro ao realizar configuração.");
        }
    }
    
    public static void conecta(GUICliente janela)
    {
        RelogioClient cont = new RelogioClient(0, janela); 
        cont.start();
        
        try
        {
            String path = System.getProperty("user.home") + "/ProjetoSD/Cliente/configs";
            FileInputStream in = new FileInputStream(path);
            Properties props = new Properties();
            props.load(in);

            String ipBanco = props.getProperty("ipBanco");
            String ipCentral = props.getProperty("ipBancoCentral");
            String nomeBanco = props.getProperty("nomeBanco");

            janela.labelCarregando.setText("Conectando ao Servidor");
            janela.setBanco(nomeBanco);

            IBanco ib = (IBanco) Naming.lookup("rmi://" + ipBanco + "/" + nomeBanco);

            try
            {
                ib.conectado();
            }
            catch(RemoteException e)
            {
                IBancoCentral central = (IBancoCentral) Naming.lookup("rmi://" + ipCentral + "/BancoCentral");
                ipBanco = central.consulta(nomeBanco, "cliente");
                ib = (IBanco) Naming.lookup("rmi://" + ipBanco + "/" + nomeBanco);
                ib.conectado();
            }
            
            janela.setServicoBanco(ib);

            janela.telaInicio();

            cont.setContinua(false);

            janela.setEnabled(true);
        }
        catch(FileNotFoundException e)
        {
            cont.setContinua(false);
            janela.aviso2("É necessário configurar o client.");
        }
        catch(RemoteException e)
        {
            cont.setContinua(false);
            janela.aviso1("Erro ao conectar ao servidor.");
        }
        catch(Exception e)
        {
            cont.setContinua(false);
            janela.aviso1("Erro ao iniciar client.");
        }
    }
    
    public static void main(String args[])
    {
        GUICliente janela = new GUICliente();

        janela.setEnabled(false);
        janela.setVisible(true);

        conecta(janela);
    }
}