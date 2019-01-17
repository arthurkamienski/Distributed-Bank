import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.util.Calendar;
import java.util.Properties;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.rmi.NotBoundException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public final class Conta extends UnicastRemoteObject implements IConta
{
    private Properties prop;
    private String ip;
    private String ipBanco;
    private String nomeBanco;
    private String arq;
    private String servico;
    private RelogioServidor cont;
    private Calendar data;

    public Conta(String ip, Properties prop, String ipBanco, String nomeBanco,
            String arq, String servico, Calendar data) throws RemoteException
    {
            this.setIp(ip);
            this.setProp(prop);
            this.setIpBanco(ipBanco);
            this.setNomeBanco(nomeBanco);
            this.setArq(arq);
            this.setServico(servico);
            this.setData(data);
    }

    private void log(String mensagem)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");

        try
        {
            FileWriter fw = new FileWriter(this.getArq() + "/log.txt", true);
            PrintWriter pw = new PrintWriter(fw, true);

            pw.println("(" + sdf.format(this.getData().getTime()) + ") " + this.getIp() + " - " + this.getProp().getProperty("numero")  + ": " + mensagem);
        }
        catch(IOException e)
        {
            System.out.println("ERRO AO ESCREVER LOG");
        }

        System.out.println("(" + sdf.format(this.getData().getTime()) + ") " + this.getIp() + ": " + mensagem);
    }

    public void extrato(String mensagem)
    {
        Calendar data = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");

        try
        {
            FileWriter fw = new FileWriter(this.getArq() + "/extrato.txt", true);
            PrintWriter pw = new PrintWriter(fw, true);

            pw.println("(" + sdf.format(data.getTime()) + ") " + mensagem);
        }
        catch(IOException e)
        {
            System.out.println("ERRO AO ESCREVER EXTRATO");
        }
    }

    @Override
    public Properties login() throws RemoteException
    {
        this.log("CLIENTE CONECTADO");
        
        this.getProp().setProperty("online", "true");
        
        RelogioServidor relogio = new RelogioServidor(this);
        
        this.setCont(relogio);
        
        cont.start();
        
        try
        {
            this.salvarProp();
        }
        catch(Exception e)
        {           }
        
        return this.getProp();
    }

    @Override
    public void logoff(boolean motivo) throws Exception
    {
        String s = "CLIENTE DESCONECTADO";
        
        if(motivo)
        {
            s += " (LOGOFF)";
            this.getCont().setContinua(false);
        }
        else
            s += " (TOKEN EXPIRADO)";
        
        this.log(s);
        
        this.getProp().setProperty("online", "false");
            
        try
        {
            this.salvarProp();
        }
        catch (Exception e)
        {
            this.log("ERRO AO DESCONECTAR CLIENTE (ERRO AO ATUALIZAR STATUS)");
        }
        
        try
        {
	    Naming.unbind(this.getServico());
            
            UnicastRemoteObject.unexportObject(this, true);
            
    	    this.log("SERVICO FECHADO");
    	}
    	catch(MalformedURLException | NotBoundException | RemoteException e)
    	{
            this.log("ERRO AO FECHAR SERVICO");
    	}
    }

    @Override
    public int consulta() throws Exception
    {
        this.log("NOVA REQUISICAO: CONSULTA A SALDO");

        int saldo = -1;

        try
        {
            this.atualizarProp();

            saldo = Integer.parseInt(this.getProp().getProperty("saldo"));

            this.log("CONSULTA A SALDO EFETUADA");
        }
        catch(Exception e)
        {
                this.log("ERRO: CONSULTA A SALDO NAO EFETUADA (ERRO AO ATUALIZAR SALDO)");
                throw new Exception("Erro ao consultar saldo.");
        }

        return saldo;
    }

    @Override
    public void saque(int valor) throws Exception
    {
        this.log("NOVA REQUISICAO: SAQUE");
        int saldo = Integer.parseInt(this.getProp().getProperty("saldo"));

        try
        {
            this.atualizarProp();

            saldo = Integer.parseInt(this.getProp().getProperty("saldo"));

            if(saldo>=valor)
            {
                this.getProp().setProperty("saldo", Integer.toString(saldo-valor));

                this.salvarProp();

                this.log("SAQUE EFETUADO");
                this.extrato("SAQUE: -" + valor);
            }
        }
        catch(Exception e)
        {
            this.log("ERRO: SAQUE NAO EFETUADO (ERRO DESCONHECIDO)");
            this.getProp().setProperty("saldo", Integer.toString(saldo));
            throw new Exception("Erro ao efetuar saque.");
        }
    }

    @Override
    public void deposito(int valor) throws Exception
    {
        this.log("NOVA REQUISICAO: DEPOSITO");

        try
        {
            this.atualizarProp();
            this.getProp().setProperty("saldo", Integer.toString(Integer.parseInt(this.getProp().getProperty("saldo"))+valor));

            this.salvarProp();

            this.log("DEPOSITO EFETUADO");
            this.extrato("DEPOSITO: +" + valor);
        }
        catch(Exception e)
        {
            this.log("ERRO: DEPOSITO NAO EFETUADO (ERRO DESCONHECIDO)");
            this.getProp().setProperty("saldo", Integer.toString(Integer.parseInt(this.getProp().getProperty("saldo"))-valor));
            throw new Exception("Erro ao efetuar deposito.");
        }
    }

    @Override
    public void transferenciaInterna(int valor, String agencia, String conta) throws Exception
    {
        this.log("NOVA REQUISICAO: TRANSFERENCIA INTERNA");
        
        String identificador = this.getProp().getProperty("numero") + "/" + this.getProp().getProperty("agencia");

        int saldo = Integer.parseInt(this.getProp().getProperty("saldo"));

        try
        {
            this.atualizarProp();

            saldo = Integer.parseInt(this.getProp().getProperty("saldo"));

            if(saldo>=valor)
            {
                this.getProp().setProperty("saldo", Integer.toString(saldo-valor));

                                IBanco banco = (IBanco) Naming.lookup("rmi://" + this.getIpBanco() + "/" + this.getNomeBanco());
                                
                                banco.depositoConta(conta, agencia, this.getIp(), identificador, valor);

                        this.salvarProp();

                    this.log("TRANSFERENCIA INTERNA EFETUADA");
            }
            else
                throw new Exception();

            this.extrato("TRANSFERENCIA PARA " + conta + ": -" + valor);
        }
        catch(Exception e)
        {
                this.log("ERRO: TRANSFERENCIA INTERNA NAO EFETUADA (ERRO DESCONHECIDO)");
                this.getProp().setProperty("saldo", Integer.toString(saldo));
                throw new Exception("Erro ao efetuar tranferencia.");
        }
    }

    @Override
    public void transferenciaExterna(int valor, String conta, String agencia, String banco, boolean doc) throws Exception
    {
        String s = "DOC";
        
        if(!doc)
            s = "TED";
        
        this.log("NOVA REQUISICAO: TRANSFERENCIA EXTERNA (" + s + ")");
        
        int saldo = Integer.parseInt(this.getProp().getProperty("saldo"));
        
        String identificador = this.getProp().getProperty("numero") + "/" + this.getProp().getProperty("agencia");

        try
        {
            this.atualizarProp();

            saldo = Integer.parseInt(this.getProp().getProperty("saldo"));

            if(saldo>=valor)
            {
                this.getProp().setProperty("saldo", Integer.toString(saldo-valor));

                IBanco servicoBanco = (IBanco) Naming.lookup("rmi://" + this.getIpBanco() + "/" + this.getNomeBanco());

                servicoBanco.depositoExterno(ip, conta, agencia, identificador, valor, banco, doc);

                this.salvarProp();

                this.log("TRANSFERENCIA EXTERNA EFETUADA(" + s + ")");
            }
            else
                throw new Exception("Saldo insuficiente.");
        }
        catch(Exception e)
        {
            this.getProp().setProperty("saldo", Integer.toString(saldo));
            
            if(e.getMessage().equals("Saldo insuficiente."))
            {
                this.log("ERRO: TRANSFERENCIA EXTERNA (" + s + ") NAO EFETUADA (SALDO INSUFICIENTE)");
            }
            else
            {
                this.log("ERRO: TRANSFERENCIA EXTERNA (" + s + ") NAO EFETUADA (ERRO DESCONHECIDO)");
                throw new Exception("Erro ao efetuar tranferencia.");
            }
        }
    }	

    @Override
    public boolean removerConta(String senha) throws Exception
    {
            boolean conectado = true;

            this.log("NOVA REQUISICAO: EXCLUSAO DE CONTA");

            if(senha.equals(this.getProp().getProperty("senha")))
                try
                {
                    IBanco servicoBanco = (IBanco) Naming.lookup("rmi://" + this.getIpBanco() + "/" + this.getNomeBanco());

                    servicoBanco.delete(this.getProp().getProperty("numero"), this.getIp());

                    conectado = false;
                }
                catch(Exception e)
                {
                        this.log("ERRO: EXCLUSAO DE CONTA NAO EFETUADA (ERRO DESCONHECIDO)");
                        throw new Exception("Erro ao excluir conta.");
                }

            return conectado;
    }
    
    @Override
    public void editarInfos(Properties novo) throws Exception
    {
        this.log("NOVA REQUISICAO: ALTERACAO DE DADOS");
        
        try
        {
            if(!this.getProp().getProperty("agencia").equals(novo.getProperty("agencia")))
            {
                File atual = new File(this.getArq());
                File agencia = new File(atual.getParentFile().getParent() + "/" + novo.getProperty("agencia"));
                if(agencia.isDirectory())
                {
                    try
                    {
                        File conta = new File(atual.getParentFile().getParent() 
                                + "/" + novo.getProperty("agencia") 
                                + "/" + novo.getProperty("numero"));
                        
                        conta.mkdir();
                        
                        Files.move(atual.toPath(), conta.toPath(), REPLACE_EXISTING);
                        this.setArq(conta.getPath());
                    }
                    catch(IOException e)
                    {
                        throw new Exception("Erro ao alterar agência.");
                    }
                }
                else
                    throw new Exception("Agência não existe.");
            }

            this.getProp().setProperty("agencia", novo.getProperty("agencia"));
            this.getProp().setProperty("nome", novo.getProperty("nome"));
            this.getProp().setProperty("cpf", novo.getProperty("cpf"));
            this.getProp().setProperty("email", novo.getProperty("email"));

            this.salvarProp();

            this.log("DADOS ALTERADOS");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            this.log("ERRO AO ALTERAR DADOS (ERRO DESCONHECIDO)");
            throw new Exception("Erro ao salvar alterações.");
        }
    }
    
    @Override
    public String getExtrato() throws Exception
    {
        String s = "";

        this.log("NOVA REQUISICAO: EXTRATO");

        try
        {
            File extrato = new File(this.getArq() + "/extrato.txt");
            FileInputStream in = new FileInputStream(extrato);
            Scanner scan = new Scanner(in);

            while(scan.hasNext())
            s += scan.nextLine() + "\n";
        }
        catch(FileNotFoundException e)
        {
            throw new Exception("Erro ao buscar extrato");
        }

        return s;
    }

    public void salvarProp() throws Exception
    {
        try
        {
            FileOutputStream out = new FileOutputStream(this.getArq() + "/dados");
            this.getProp().store(out, null);
            out.close();
        }
        catch(IOException e)
        {
            throw new Exception("Erro ao salvar dados");
        }
    }

    public void atualizarProp() throws Exception
    {
        try
        {
            FileInputStream in = new FileInputStream(this.getArq() + "/dados");
            this.getProp().load(in);
        }
        catch(IOException e)
        {
            throw new Exception("Erro ao atualizar dados");
        }
    }

    public void setIp(String ip)
    {
            this.ip = ip;
    }

    public void setIpBanco(String ipBanco)
    {
            this.ipBanco = ipBanco;
    }

    public void setNomeBanco(String nomeBanco)
    {
            this.nomeBanco = nomeBanco;
    }
    
    public void setArq(String arq)
    {
        this.arq = arq;
    }	

    public void setProp(Properties prop)
    {
            this.prop = prop;
    }

    public void setServico(String servico)
    {
        this.servico = servico;
    }

    public Properties getProp()
    {
            return this.prop;
    }

    public String getIp()
    {
            return this.ip;
    }

    public String getIpBanco()
    {
            return this.ipBanco;
    }

    public String getNomeBanco()
    {
            return this.nomeBanco;
    }

    public String getArq()
    {
        return this.arq;
    }
    
    public String getServico()
    {
        return this.servico;
    }

    private void setCont(RelogioServidor cont)
    {
        this.cont = cont;
    }
    
    private RelogioServidor getCont()
    {
        return this.cont;
    }

    public Calendar getData()
    {
        return data;
    }

    public void setData(Calendar data)
    {
        this.data = data;
    }
}