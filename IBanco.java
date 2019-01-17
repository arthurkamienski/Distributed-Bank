import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Calendar;

public interface IBanco extends Remote
{
	public void desconectado() throws RemoteException;
	public void conectado() throws RemoteException;
	public String acessarConta(String agencia, String cpf, String senha) throws Exception;
	public int criarConta(String cpf, String nome, String email, String senha) throws Exception;
	public void depositoConta(String conta, String agencia, String ip, String identificador, int valor) throws Exception;
	public void depositoExterno(String ip, String conta, String agencia, String identificador, int valor, String banco, boolean doc) throws Exception;
	public void delete(String conta, String ip)throws Exception;
    public void fazerTransfs() throws RemoteException;
    public void setData(Calendar data) throws RemoteException;
    public void abrirAgencia(String numero) throws RemoteException;
}