
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import java.util.Scanner;

//java -Dpi4j.linking=dynamic -cp MagnetJ.jar Main

public class Main
{
    private static GpioController gpio = GpioFactory.getInstance();
    private static Connection connection;
    private static GpioPinDigitalOutput pinLaser;
    private static GpioPinDigitalInput pinMagnet, pinTilt;
    private static Thread threadLaser, threadMagnet, threadTilt;
    
    public static void main(String[] args) {
        connect();
    }
    
    private static void connect(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter address of broker. Example: tcp://192.168.137.1:1883");
        String ip = sc.nextLine();
        connection = new Connection(new Client(), ip);
    }
    
    private static class Client implements ClientListener{
        
        public void messageArrived(String topic, String message){
            switch (topic){ 
                case Connection.LASER_SENSOR: { 
                    System.out.println("Message: " + message);
                    if (message.equals("1")){
                        pinLaser.high();
                    } else {
                        pinLaser.low();
                    }
                    break;
                } 
            }
        }
        
        public void connected(){
            setSensors();
        }
        
        public void connectionFailed(){
            threadLaser.stop();
            threadMagnet.stop();
            threadTilt.stop();
            connect();
        }
    }
    
    private static void setSensors(){
        threadMagnet = new Thread(new Runnable() { 
            public void run() { 
                connectToMagnet(); 
            } 
        }); 
        threadMagnet.start();
        
        threadLaser = new Thread(new Runnable() { 
            public void run() { 
                connectToLaser(); 
            } 
        }); 
        threadLaser.start();
        
        threadTilt = new Thread(new Runnable() { 
            public void run() { 
                connectToTilt(); 
            } 
        }); 
        threadTilt.start();
    }

    private static void connectToMagnet() {
        if (pinMagnet == null) {
            pinMagnet = gpio.provisionDigitalInputPin(RaspiPin.GPIO_08);
            pinMagnet.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        }
        while (true){
            PinState state = pinMagnet.getState();
            connection.sendMessage(Connection.MAGNET_SENSOR, String.valueOf(state.getValue()));
            try { 
                Thread.sleep(100); 
            } catch (InterruptedException e) { 
                System.out.println("Thread.sleep error: " + e.toString()); 
            }
        }
    }
    
    private static void connectToLaser(){
        System.out.println("Connect to laser");
        if (pinLaser == null) {
            pinLaser = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);
            pinLaser.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        }
        System.out.println("Laser connected");
        pinLaser.addListener(new GpioPinListenerDigital() { 
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) { 
                connection.sendMessage(Connection.LASER_CLIENT, String.valueOf(gpioPinDigitalStateChangeEvent.getState().getValue()));
            } 
        });
    }
    
    private static void connectToTilt(){
        if (pinTilt == null) {
            pinTilt = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04);
            pinTilt.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        }
        while (true){
            PinState state = pinTilt.getState();
            connection.sendMessage(Connection.TILT_CLIENT, String.valueOf(state.getValue()));
            try { 
                Thread.sleep(100); 
            } catch (InterruptedException e) { 
                System.out.println("Thread.sleep error: " + e.toString()); 
            }
        }
    }

}
