import java.rmi.Remote;
import java.util.Properties;

public interface IConta extends Remote
{
	public int consulta() throws Exception;
	public void saque(int valor) throws Exception;
	public void deposito(int valor) throws Exception;
	public void transferenciaInterna(int valor, String agencia, String conta) throws Exception;
	public void transferenciaExterna(int valor, String agencia, String conta, String banco, boolean doc) throws Exception;
	public Properties login() throws Exception;
	public void logoff(boolean motivo) throws Exception;
        public String getExtrato() throws Exception;
	public boolean removerConta(String senha) throws Exception;
        public void editarInfos(Properties prop) throws Exception;
}