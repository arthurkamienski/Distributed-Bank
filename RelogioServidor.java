
import java.util.Calendar;

public final class RelogioServidor extends Thread
{ 
    private int selec;
    private boolean continua;
    private Conta conta;
    private Banco banco;
    
    public RelogioServidor(Conta conta)
    {
        this.setSelec(0);
        this.setConta(conta);
        this.setContinua(true);
    }
    
    public RelogioServidor(Banco banco)
    {
        this.setSelec(1);
        this.setBanco(banco);
        this.setContinua(true);
    }
    
    @Override
    public void run()
    {  
        switch (this.getSelec())
        {
            case 0:
                try
                {
                    int min = 5;
                    int seg = 10;
                    
                    while((min != 0 || seg != 0) && this.isContinua())
                    {
                        Thread.sleep(1000);
                        
                        seg--;
                        
                        if(seg == -1)
                        {
                            seg = 59;
                            min--;
                        }
                    }
                    try
                    {
                        if(this.isContinua())
                            this.getConta().logoff(false);
                    }
                    catch (Exception ex)
                    {           }
                }
                catch (InterruptedException ex)
                {            }
                break;
            case 1:
                try
                {
                    int hora = this.getBanco().getData().get(Calendar.HOUR_OF_DAY);
                    
                    while(this.isContinua())
                    {
                        Thread.sleep(1000);
                        this.getBanco().getData().setTimeInMillis(this.getBanco().getData().getTimeInMillis() + 1000);
                        
                        if(this.getBanco().getData().get(Calendar.HOUR_OF_DAY) !=  hora)
                        {
                            hora = this.getBanco().getData().get(Calendar.HOUR_OF_DAY);
                            if(this.getBanco().getData().get(Calendar.HOUR_OF_DAY) ==  9)
                                this.getBanco().fazerTransfs();
                        }
                    }
                }
                catch(InterruptedException ex)
                { }
                catch(Exception e)
                { }
                break;
        }
    }
    
    public Banco getBanco()
    {
        return banco;
    }

    public void setBanco(Banco banco)
    {
        this.banco = banco;
    }
    
    public void setContinua(boolean continua)
    {
        this.continua = continua;
    }
    
    public boolean isContinua()
    {
        return this.continua;
    }
    
    public void setSelec(int selec)
    {
        this.selec = selec;
    }
    
    public int getSelec()
    {
        return this.selec;
    }

    public Conta getConta()
    {
        return conta;
    }

    public void setConta(Conta conta)
    {
        this.conta = conta;
    }
}
