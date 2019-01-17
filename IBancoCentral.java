import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;
import java.io.FileNotFoundException;

public interface IBancoCentral extends Remote
{
	public void novoBanco(String nome) throws Exception;
	public String consulta(String nome, String identificador) throws Exception;
}