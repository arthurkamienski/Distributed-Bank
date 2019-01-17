public final class RelogioClient extends Thread
{
    private int selec;
    private GUICliente janela;
    private boolean continua;
    
    public RelogioClient(int selec, GUICliente janela)
    {
        this.setJanela(janela);
        this.setSelec(selec);
        this.setContinua(true);
    }

    @Override
    public void run()
    {  
        switch (this.getSelec())
        {
            case 0:
                while(this.isContinua())
                {
                    if(!this.getJanela().pontos.getText().equals("..."))
                        this.getJanela().pontos.setText(getJanela().pontos.getText() + ".");
                    else
                        this.getJanela().pontos.setText(".");
                    
                    try
                    {
                        RelogioClient.sleep(1000);
                    }
                    catch (InterruptedException ex)
                    { }
                }   break;
            case 1:
                try
                {
                    int min = 5;
                    int seg = 0;
                    
                    this.getJanela().escreve(min + ":0" + seg);
                    
                    while(min != 0 || seg != 0)
                    {
                        RelogioClient.sleep(1000);
                        
                        seg--;
                        
                        if(seg == -1)
                        {
                            seg = 59;
                            min--;
                        }
                        
                        if(seg < 10)
                            this.getJanela().escreve(min + ":0" + seg);
                        else
                            this.getJanela().escreve(min + ":" + seg);
                    }
                    
                    this.getJanela().sair();
                }
                catch (InterruptedException ex)
                {            }
                break;
        }
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
    
    public void setJanela(GUICliente janela)
    {
        this.janela = janela;
    }
    
    public GUICliente getJanela()
    {
        return this.janela;
    }
}
