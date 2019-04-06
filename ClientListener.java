
public interface ClientListener
{
    void messageArrived(String topic, String message);
    void connected();
    void connectionFailed();
}
