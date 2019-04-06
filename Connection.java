
import org.eclipse.paho.client.mqttv3.MqttClient; 
import org.eclipse.paho.client.mqttv3.MqttException; 
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

public class Connection
{
    private static final String SENSOR_TOPIC = "/sensor"; 
    private static final String CLIENT_TOPIC = "/client"; 
    public static final String LASER_SENSOR = SENSOR_TOPIC + "/laser"; 
    public static final String LASER_CLIENT = CLIENT_TOPIC + "/laser"; 
    public static final String MAGNET_SENSOR = SENSOR_TOPIC + "/magnet";
    public static final String TILT_CLIENT = CLIENT_TOPIC + "/tilt"; 
    private MqttClient client;
    private ClientListener listener;

    public Connection(ClientListener listener, String address) { 
        this.listener = listener;
        System.out.println("== START PUBLISHER =="); 
        try { 
            client = new MqttClient(address, MqttClient.generateClientId()); 
            client.connect(); 
            listener.connected();
            System.out.println("== CONNECTED =="); 
            client.setCallback(new MqttCallback() { 
                public void connectionLost(Throwable throwable) { 
                    System.out.println("ERROR Connection Lost: " + throwable.toString()); 
                    listener.connectionFailed();
                } 
                
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception { 
                    listener.messageArrived(s, mqttMessage.toString()); 
                } 
                
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) { 
                } 
            }); 
            client.subscribe(LASER_SENSOR);
        } catch (MqttException e) { 
            System.out.println("Connection error: " + e.toString()); 
            listener.connectionFailed();
            return;
        } 
        
    } 

    public void sendMessage(String topic, String message){ 
        MqttMessage mqttMessage = new MqttMessage(); 
        mqttMessage.setPayload(message.getBytes()); 
        try { 
            client.publish(topic, mqttMessage); 
        } catch (MqttException e) { 
            System.out.println("Publish ERROR: " + e.toString()); 
            return; 
        } 
        System.out.println("Arrived: " + topic + " - " + message); 
    } 
    
    public void disconnect(){ 
        try { 
            client.disconnect(); 
        } catch (MqttException e) { 
            e.printStackTrace(); 
        } 
        System.out.println("== END PUBLISHER =="); 
    }
}
