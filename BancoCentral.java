import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Properties;
import java.text.SimpleDateFormat;

public final class BancoCentral extends UnicastRemoteObject implements IBancoCentral
{   
    private String ip;
    private Properties prop;
    private File localBancos;
    private File log;

    public BancoCentral(String ip, Properties prop, File bancos, File log) throws RemoteException
    {
        this.setIp(ip);
        this.setLocalBancos(bancos);
        this.setProp(prop);
        this.setArqLog(log);
    }

    public void log(String mensagem)
    {
        Calendar data = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");

        try
        {
            FileWriter fw = new FileWriter(this.getArqLog(), true);
            PrintWriter pw = new PrintWriter(fw, true);

            pw.println("(" + sdf.format(data.getTime()) + ") " + mensagem);
        }
        catch(Exception e)
        {
            System.out.println("ERRO AO ESCREVER LOG");
        }

        System.out.println("(" + sdf.format(data.getTime()) + ") " + mensagem);
    }

    public void novoBanco(String nome) throws Exception
    {
        String ipRequisicao;

        try
        {
            ipRequisicao = this.getClientHost();

            this.log("NOVO BANCO ONLINE: " + nome + " (" + ipRequisicao + ").");

            if(this.getProp().getProperty(nome, "na").equals("na"))
            {
                this.log(nome + ": BANCO NAO REGISTRADO. REGISTRANDO BANCO");

                this.getProp().setProperty(nome, ipRequisicao);

                FileOutputStream out = new FileOutputStream(this.getLocalBancos());
                this.getProp().store(out, null);
                out.close();

                this.log("NOVO BANCO REGISTRADO: " + nome);
            }
            else
            {
                if(ipRequisicao.equals(this.getProp().getProperty(nome)))
                    this.log(nome + ": REGISTRO OK");
                else
                {
                    this.log(nome + ": IPS DISTINTOS. SOBRESCREVENDO");

                    this.getProp().setProperty(nome, ipRequisicao);

                    FileOutputStream out = new FileOutputStream(this.getLocalBancos());
                    this.getProp().store(out, null);
                    out.close();

                    this.log(nome + ": NOVO IP REGISTRADO");
                }
            }
        }
        catch(Exception e)
        {
            this.log("ERRO: ERRO AO REGISTRAR NOVO BANCO (ERRO DESCONHECIDO)");
            throw new Exception();
        }
    }

    public String consulta(String nome, String identificador) throws Exception
    {
        String ip;

        try
        {
            ip = this.getClientHost();
        }
        catch(Exception e)
        {
            ip = "DESCONHECIDO";
        }

        this.log(ip + ": (" + identificador + ") NOVA CONSULTA: " + nome);

        try
        {
            ip = this.getProp().getProperty(nome);

            if(ip.equals(null))
                throw new Exception();
        }
        catch(Exception e)
        {
            throw new Exception();
        }

        return ip;
    }

    public File getLocalBancos()
    {
        return this.localBancos;
    }

    public void setLocalBancos(File localBancos)
    {
        this.localBancos = localBancos;
    }

    public String getIp()
    {
        return this.ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public File getArqLog()
    {
        return this.log;
    }

    public void setArqLog(File log)
    {
        this.log = log;
    }

    public Properties getProp()
    {
        return this.prop;
    }

    public void setProp(Properties prop)
    {
        this.prop = prop;
    }
}