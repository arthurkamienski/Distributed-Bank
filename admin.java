
import java.rmi.Naming;
import java.util.Calendar;
import java.util.Scanner;

public class admin
{
    public static void main(String args[])
    {
        Scanner scan = new Scanner(System.in);
        
        System.out.println("Nome do banco: ");
        String nomeBanco = scan.nextLine();
        
        System.out.println("IP do banco: ");
        String ipBanco = scan.nextLine();
        
        try
        {
            IBanco ib = (IBanco) Naming.lookup("rmi://" + ipBanco + "/" + nomeBanco);
            
            System.out.println("Digite o comando:");
            System.out.println("1 - Realizar Transfs");
            System.out.println("2 - Alterar hora/data");
            System.out.println("3 - Criar agência");
            System.out.println("0 - Sair");
            
            String resposta = scan.nextLine();
            
            while(!resposta.equals("0"))
            {
                if(resposta.equals("1"))
                {
                    System.out.println("Fazendo transferencias");
                    ib.fazerTransfs();
                }
                else if(resposta.equals("2"))
                {
                    Calendar data = Calendar.getInstance();
                    
                    System.out.println("Dia:");
                    data.set(Calendar.DAY_OF_MONTH, Integer.parseInt(scan.nextLine()));
                    System.out.println("Mes:");
                    data.set(Calendar.MONTH, Integer.parseInt(scan.nextLine()));
                    System.out.println("Hora:");
                    data.set(Calendar.HOUR_OF_DAY, Integer.parseInt(scan.nextLine()));
                    System.out.println("Minuto:");
                    data.set(Calendar.MINUTE, Integer.parseInt(scan.nextLine()));
                    
                    ib.setData(data);
                }
                else if(resposta.equals("3"))
                {
                    System.out.println("Digite o numero da agencia");
                    String numero = scan.nextLine();
                    ib.abrirAgencia(numero);
                }
                
                resposta = scan.nextLine();
            }
                
        }
        catch(Exception e)
        {
            System.out.println("Erro ao contactar banco");
        }
    }
}
