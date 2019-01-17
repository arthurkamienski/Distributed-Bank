import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.rmi.server.ServerNotActiveException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

public final class Banco extends UnicastRemoteObject implements IBanco
{   
    private String nome;
    private String ip;
    private String ipBancoCentral;
    private String servico;
    private File log;
    private File localContas;
    private File transfs;
    private Calendar data;
    
    public Banco(String nome, String ipBancoCentral, String ip, String servico,
            File contas, File log, File transfs) throws RemoteException
    {
        this.setNome(nome);
        this.setIpBancoCentral(ipBancoCentral);
        this.setIp(ip);
        this.setServico(servico);
        this.setLocalContas(contas);
        this.setArqLog(log);
        this.setTransfs(transfs);
        
        Calendar dataAtual = Calendar.getInstance();
        this.setData(dataAtual);
        RelogioServidor rel = new RelogioServidor(this);
        rel.start();
    }

    public void log(String mensagem)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");

        try
        {
            FileWriter fw = new FileWriter(this.getArqLog(), true);
            PrintWriter pw = new PrintWriter(fw, true);

            pw.println("(" + sdf.format(this.getData().getTime()) + ") " + mensagem);
        }
        catch(IOException e)
        {
            System.out.println("ERRO AO ESCREVER LOG");
        }

        System.out.println("(" + sdf.format(this.getData().getTime()) + ") " + mensagem);
    }
    
    @Override
    public void conectado() throws RemoteException
    {
        String mensagem = "NOVO ACESSO: ";

        try
        {
            mensagem += Banco.getClientHost();
        }
        catch(ServerNotActiveException e)
        {
            mensagem += "DESCONHECIDO";
        }

        log(mensagem);
    }

    @Override
    public void desconectado() throws RemoteException
    {
        String mensagem;

        try
        {
            mensagem = Banco.getClientHost();
        }
        catch(ServerNotActiveException e)
        {
            mensagem = "DESCONHECIDO";
        }

        mensagem += " DESCONECTADO";

        log(mensagem);    
    }

    @Override
    public String acessarConta(String agencia, String numeroConta, String senha) throws Exception
    {
        Random rand = new Random(System.currentTimeMillis());
        int token = rand.nextInt();
        String servicoConta;
        String ipCliente;
        int erro = 0;

        try
        {
            ipCliente = Banco.getClientHost();
        }
        catch(ServerNotActiveException e)
        {
            ipCliente = "DESCONHECIDO";
        }

        log(ipCliente + ": NOVA REQUISICAO: ACESSO A CONTA " + numeroConta);

        try
        {
            String dirConta = this.getLocalContas().getPath() + "/" + agencia + "/" + numeroConta;
            servicoConta = this.getServico() + "/" + agencia + "/" + numeroConta + "/" + token;

            FileInputStream in;
            Properties prop = new Properties();
            
            try
            {
                in = new FileInputStream(dirConta + "/dados");
            }
            catch(IOException e)
            {
                dirConta = this.getLocalContas().getPath() + "/naodef/" + numeroConta;
                servicoConta = this.getServico() +  "/naodef/" + numeroConta + "/" + token;
                in = new FileInputStream(dirConta + "/dados");
            }
            
            prop.load(in);
            
            if(prop.getProperty("online").equals("false"))
            {
                if(senha.equals(prop.getProperty("senha")))
                {
                    Conta conta = new Conta(ipCliente, prop, this.getIp(), this.getNome(),
                            dirConta, servicoConta, this.getData());

                    try
                    {
                        Naming.rebind(servicoConta, conta);

                        log(ipCliente + ": CONTA " + numeroConta + " ACESSADA");
                    }
                    catch(MalformedURLException | RemoteException e)
                    {
                        erro = -2;
                        throw new Exception();           
                    }
                }
                else
                {
                    erro = -1;
                    throw new Exception();
                }
            }
            else
            {
                erro = -3;
                throw new Exception();
            }
        }
        catch(RemoteException e)
        {
            log(ipCliente + ": ERRO DE CONEXAO");
            throw new Exception("Erro de conexao com servidor");
        }
        catch(FileNotFoundException e)
        {
            log(ipCliente + ": ERRO AO ACESSAR CONTA (ARQUIVO CONTA NAO ENCONTRADO)");
            throw new Exception("Conta nao existe.");
        }
        catch(Exception e)
        {
            String mensagem;

            switch (erro) {
                case -1:
                    log(ipCliente + ": ERRO AO ACESSAR CONTA (SENHA INCORRETA)");
                    mensagem = "Senha incorreta.";
                    break;
                case -2:
                    log(ipCliente + ": ERRO AO ACESSAR CONTA (ERRO AO CRIAR SERVICO)");
                    mensagem = "Erro no servidor";
                    break;
                case -3:
                    log(ipCliente + ": ERRO AO ACESSAR CONTA (CLIENTE JA CONECTADO)");
                    mensagem = "Cliente já conectado.";
                    break;
                default:
                    log(ipCliente + ": ERRO AO ACESSAR CONTA (ERRO DESCONHECIDO)");
                    mensagem = "Erro desconhecido.";
                    break;
            }

            throw new Exception(mensagem);
        }

        return servicoConta;
    }

    @Override
    public int criarConta(String cpf, String nome, String email, String senha) throws Exception
    {
        int erro = 0;
        String ipCliente;
        int numeroConta = Math.abs(cpf.hashCode())%1000000;

        try
        {
            ipCliente = Banco.getClientHost();
        }
        catch(ServerNotActiveException e)
        {
            ipCliente = "DESCONHECIDO";
        }

        log(ipCliente + ": NOVA REQUISICAO: CRIAR CONTA");

        try
        {
            File novo = new File(this.getLocalContas().getPath() + "/naodef/" + numeroConta);

            if(!novo.exists())
            {
                novo.mkdirs();

                File extrato = new File(novo.getPath() + "/extrato.txt");
                extrato.createNewFile();
                
                FileOutputStream out = new FileOutputStream(novo.getPath() + "/dados");

                Properties prop = new Properties();

                prop.setProperty("numero", Integer.toString(numeroConta));
                prop.setProperty("senha", senha);
                prop.setProperty("nome", nome);
                prop.setProperty("email", email);
                prop.setProperty("saldo", "0");
                prop.setProperty("cpf", cpf);
                prop.setProperty("online", "false");
                prop.setProperty("agencia", "naodef");
                prop.store(out, null);
                out.close();

                log(ipCliente + ": NOVA CONTA CRIADA: " + numeroConta);
            }
            else
            {
                erro = -1;
                throw new Exception();
            }
        }
        catch(RemoteException e)
        {
            log(ipCliente + ": ERRO AO CRIAR CONTA (ERRO DE CONEXAO)");
            throw new Exception("Erro de conexao com servidor");
        }
        catch(IOException e)
        {
            log(ipCliente + ": ERRO AO CRIAR CONTA (FALHA AO CRIAR ARQUIVO)");
            throw new Exception("Erro ao registrar conta.");
        }
        catch(Exception e)
        {
            String mensagem;

            if(erro == -1)
            {
                log(ipCliente + ": ERRO AO CRIAR CONTA (CONTA JA EXISTENTE)");
                throw new Exception("Cliente já cadastrado");
            }
            else            
            {
                log(ipCliente + ": ERRO AO CRIAR CONTA (ERRO DESCONHECIDO)");
                mensagem = "Erro desconhecido.";
            }

            throw new Exception(mensagem);
        }

        return numeroConta;
    }

    @Override
    public void depositoConta(String conta, String agencia, String ip, String identificador, int valor) throws Exception
    {
        log(ip + ": (" + identificador + ") NOVA REQUISICAO: DEPOSITO NA CONTA " + conta);

        try
        {
            String local = this.getLocalContas().getPath() + "/" + agencia + "/" + conta + "/dados";
            
            FileInputStream in = new FileInputStream(local);
            Properties prop = new Properties();
            prop.load(in);


            prop.setProperty("saldo", Integer.toString(Integer.parseInt(prop.getProperty("saldo"))+valor));
        
            FileOutputStream out = new FileOutputStream(local);
            prop.store(out, null);

            out.close();

            Calendar dataTransf = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");

            try
            {
                FileWriter fw = new FileWriter(this.getLocalContas().getPath() + "/" + agencia + "/" + conta + "/extrato.txt", true);
                PrintWriter pw = new PrintWriter(fw, true);

                pw.println("(" + sdf.format(dataTransf.getTime()) + ") TRANSFERENCIA DE " + identificador + ": +" + valor);
            }
            catch(IOException e)
            {
                System.out.println("ERRO AO ESCREVER EXTRATO");
            }

            log(ip + ": (" + identificador + ") DEPOSITO NA CONTA " + agencia + "/" + conta + " EFETUADO");
        }
        catch(RemoteException e)
        {
            log(ip + ": ERRO AO REALIZAR DEPOSITO (ERRO NO SERVIDOR)");
            throw new Exception("Erro de conexao com servidor");
        }
        catch(FileNotFoundException e)
        {
            log(ip + ": ERRO AO REALIZAR DEPOSITO (ARQUIVO NAO ENCONTRADO)");
            throw new FileNotFoundException("Conta não encontrada.");    
        }
        catch(IOException e)
        {
            log(ip + ": ERRO AO REALIZAR DEPOSITO (ERRO AO ATUALIZAR SALDO)");
            throw new Exception("Erro ao acessar conta.");
        }
        catch(Exception e)
        {
            log(ip + ": ERRO AO REALIZAR DEPOSITO (ERRO DESCONHECIDO)");
            System.out.println(e.getMessage());
            throw new Exception("Erro desconhecido.");
        }
    }

    @Override
    public void depositoExterno(String ip, String conta, String agencia, String identificador, int valor, String banco, boolean doc) throws Exception
    {
        String s = ip + ": (" + identificador + ") NOVA REQUISICAO: DEPOSITO EXTERNO";
        
        if(doc)
            s += " (DOC)";
        else
            s += " (TED)";
        
        s += "(BANCO: " + banco + "/CONTA: " + conta;

        log(s);
        
        if(doc || this.getData().get(Calendar.HOUR_OF_DAY)>=17)
        {   
            int dia = this.getData().get(Calendar.DAY_OF_MONTH) + 1;
            int mes = this.getData().get(Calendar.MONTH);
            
            String local = this.getTransfs().getPath() + "/" + dia + "." + mes;
            File pasta = new File(local);
            
            if(!pasta.exists())
                pasta.mkdirs();
            
            try
            {
                File transf = new File(pasta.getPath() + "/" + System.currentTimeMillis());
                Properties prop = new Properties();

                prop.setProperty("conta", conta);
                prop.setProperty("agencia", agencia);
                prop.setProperty("identificador", identificador);
                prop.setProperty("valor", Integer.toString(valor));
                prop.setProperty("banco", banco);

                FileOutputStream out = new FileOutputStream(transf);
                prop.store(out, null);
                out.close();

                log(ip + ": (" + identificador + ") DEPOSITO EXTERNO AGENDADO ("
                        + dia + "/"
                        + mes
                        + ") (BANCO: " + banco + "/CONTA: " + conta);
            }
            catch(RemoteException e)
            {
                log(ip + ": (" + conta + ") ERRO AO AGENDAR DEPOSITO EXTERNO (ERRO AO CONTACTAR BANCO)");
                throw new Exception("Erro ao agendar transferência.");
            }
            catch(Exception e)
            {
                log(ip + ": (" + conta + ") ERRO AO AGENDAR DEPOSITO EXTERNO (ERRO DESCONHECIDO)");
                throw new Exception("Erro ao agendar transferência.");
            }
        }
        else
        {
            transferir(conta, agencia, identificador, valor, banco);
            log(ip + ": (" + identificador + ") DEPOSITO EXTERNO REALIZADO (BANCO: " 
                    + banco + "/CONTA: " + conta);
        }
    }
    
    private void transferir(String conta, String agencia, String identificador, int valor, String banco) throws Exception
    {
        try
        {
            IBanco servicoBanco;
            IBancoCentral bancoCentral;
            
            bancoCentral = (IBancoCentral) Naming.lookup("rmi://" + this.getIpBancoCentral() + "/BancoCentral");

            String ipBanco = bancoCentral.consulta(banco, this.getNome());

            servicoBanco = (IBanco) Naming.lookup("rmi://" + ipBanco + "/" + banco);

            servicoBanco.depositoConta(conta, agencia, this.getIp(), this.getNome() + "//" + identificador, valor);
        }
        catch(RemoteException e)
        {
            log(ip + ": (" + conta + ") ERRO AO REALIZAR DEPOSITO EXTERNO (ERRO AO CONTACTAR BANCO)");
            e.printStackTrace();
            throw new Exception("Erro ao contactar banco");
        }
        catch(Exception e)
        {
            log(ip + ": (" + conta + ") ERRO AO REALIZAR DEPOSITO EXTERNO (ERRO DESCONHECIDO)");
            throw e;
        }
    }
    
    @Override
    public void fazerTransfs() throws RemoteException
    {
        Properties props = new Properties();

        int dia = this.getData().get(Calendar.DAY_OF_MONTH) + 1;
        int mes = this.getData().get(Calendar.MONTH);
        
        File arq = new File(this.getTransfs().getPath() + "/"
                + dia + "." + mes);
        
        for (final File transf : arq.listFiles())
        {
            try
            {
                FileInputStream in = new FileInputStream(transf);
                props.load(in);
                
                transferir(props.getProperty("conta"), props.getProperty("agencia"),
                props.getProperty("identificador"),
                Integer.parseInt(props.getProperty("valor")),
                props.getProperty("banco"));
                
                in.close();
            }
            catch(FileNotFoundException e)
            { }
            catch(Exception e)
            {
                try
                {
                    depositoExterno(this.getIp(), props.getProperty("conta"), props.getProperty("agencia"),
                    props.getProperty("identificador"),
                    Integer.parseInt(props.getProperty("valor")),
                    props.getProperty("banco"), true);
                }
                catch (Exception ex)
                {   }
            }
                transf.delete();            
        }
        
        arq.delete();
    }

    @Override
    public void delete(String conta, String ip) throws RemoteException, Exception
    {
        log(ip + ": (" + conta + ") NOVA REQUISICAO: EXCLUIR CONTA");

        try
        {
            File arquivo = new File(this.getLocalContas().getPath() + "/" + conta + "/dados");
            arquivo.delete();

            arquivo = new File(this.getLocalContas().getPath() + "/" + conta + "/extrato.txt");
            arquivo.delete();

            arquivo = new File(this.getLocalContas().getPath() + "/" + conta + "/log.txt");
            arquivo.delete();

            arquivo = new File(this.getLocalContas().getPath() + "/" + conta);
            arquivo.delete();

            log(ip + ": (" + conta + ") CONTA EXCLUIDA");
        }
        catch(Exception e)
        {
            log(ip + ": (" + conta + ") ERRO AO EXCLUIR CONTA (ERRO DESCONHECIDO)");
            throw new Exception("ERRO AO EXCLUIR CONTA");
        }
    }
    
    @Override
    public void abrirAgencia(String numero) throws RemoteException
    {
        File nova = new File(this.getLocalContas().getPath() + "/" + numero);
        
        if(!nova.exists())
            nova.mkdir();
    }

    public File getLocalContas()
    {
        return this.localContas;
    }

    public void setLocalContas(File localContas)
    {
        this.localContas = localContas;
    }

    public String getNome()
    {
        return this.nome;
    }

    public void setNome(String nome)
    {
        this.nome = nome;
    }

    public String getIpBancoCentral()
    {
        return this.ipBancoCentral;
    }

    public void setIpBancoCentral(String ipBancoCentral)
    {
        this.ipBancoCentral = ipBancoCentral;
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

    public String getServico()
    {
        return this.servico;
    }

    public void setServico(String servico)
    {
        this.servico = servico;
    }

    public File getTransfs()
    {
        return transfs;
    }

    public void setTransfs(File transfs)
    {
        this.transfs = transfs;
    }

    public Calendar getData()
    {
        return data;
    }

    public void setData(Calendar data) throws RemoteException
    {
        this.data = data;
    }
}