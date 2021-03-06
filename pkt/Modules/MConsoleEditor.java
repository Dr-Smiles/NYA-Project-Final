package pkt.Modules;

import org.jline.terminal.Cursor;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import pkt.Interface.IConsole;

import java.io.IOException;
import java.text.DecimalFormat;

public class MConsoleEditor implements IConsole {
    
    private Terminal terminal;
    private Cursor cursorLocation;
    private LineReader reader;
    private static final DecimalFormat newDoubleType = new DecimalFormat("0.000");
    
    private static int main_menu_oldSelection = 0;
    static private int staticMenuType = 0;

    public MConsoleEditor(){
        try {
            terminal = TerminalBuilder.terminal();
            //terminal.enterRawMode();
            reader = LineReaderBuilder.builder().terminal( terminal ).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gotoxy(int satir, int sutun){terminal.puts(InfoCmp.Capability.cursor_address, satir, sutun);}
    private void cout(String s){System.out.print(s);}
    
    private void generateBorders(int satir,int sutun){
        int x,y;
        for(y = 0; y<sutun; y++){
            for(x = 0; x<satir; x++){
                if ((y==0 | y==sutun-1) | (x==0 | x == satir-1)){
                    gotoxy(x, y);
                    System.out.print("\u2588");
                }
            }
        }
    }
    
    @Override
    public void clearConsole(){System.out.print("\033[H\033[2J");}

    @Override
    public void thread_Update(double room_temp, double target_temp) {

        switch(staticMenuType){
            case 1:
            gotoxy(2, 0);
            cout("\t Hedef Sıcaklık: " + newDoubleType.format(target_temp) + " °C | Oda Sıcaklığı: " + newDoubleType.format(room_temp) + " °C");
            gotoxy(cursorLocation.getY(), cursorLocation.getX());
            break;
            case 2:
            gotoxy(2, 0);
            cout("                                                                                            ");
            gotoxy(2, 0);
            cout("\t\t\t\t IDLE MODE");
            gotoxy(cursorLocation.getY(), cursorLocation.getX());
            break;
            default:
            gotoxy(2, 0);
            cout("\t Hedef Sıcaklık: " + newDoubleType.format(target_temp) + " °C | Oda Sıcaklığı: " + newDoubleType.format(room_temp) + " °C");
            gotoxy(cursorLocation.getY(), cursorLocation.getX());
            break;
        }
    }

    @Override
    public void thread_MenuType(int menu_type) {
        staticMenuType = menu_type;
    }

    @Override
    public int menu_main(int tempModStatus) {
        clearConsole();
        generateBorders(15, 75);
        thread_MenuType(1);
        generateBorders(5, 75);

        gotoxy(6, 0);
        cout("\t 1 - Hedef Sıcaklık Ayarlama");
        
        gotoxy(7, 0);
        cout((tempModStatus != 1) ? "\t 2 - Soğutucuyu Aç" : "\t 2 - Soğutucuyu Kapat");

        gotoxy(8, 0);
        cout((tempModStatus != 2) ? "\t 3 - Isıtıcıyı Aç" : "\t 3 - Isıtıcıyı Kapat");


        gotoxy(9, 0);
        cout("\t 5 - Kapat");

        gotoxy(11, 0);
        cout("\t Seçiminiz:");
        cursorLocation = terminal.getCursorPosition(null);
        main_menu_oldSelection = Integer.parseInt(reader.readLine(" "), 0, 1, 10);
        return main_menu_oldSelection;
    }

    private static int[] prev_loginAttempt = {0,0};

    @Override
    public String menu_login(int login_step) {
        if (login_step == 1){
            clearConsole();
            generateBorders(5, 65);
            gotoxy(2, 0);
            cout((prev_loginAttempt[0] == 0) ? "\t Kullanıcı Adınızı Giriniz:" : "\t Kullanıcı Adınızı Tekrar Giriniz:");
            prev_loginAttempt[0] = 1;
            return reader.readLine(" ");
        }
        else {
            clearConsole();
            generateBorders(5, 65);
            gotoxy(2, 0);
            cout((prev_loginAttempt[1] == 0) ? "\t Şifrenizi Giriniz:" : "\t Şifrenizi Tekrar Giriniz:");
            prev_loginAttempt[1] = 1;
            return reader.readLine(" ");
        }
    }

    @Override
    public String menu_connectionType(){
        clearConsole();
        generateBorders(9, 65);
        gotoxy(2, 0);
        cout("\t Select a connection type:");
        
        gotoxy(4, 0);
        cout("\t Virtual Server Connection (Virtual)");
        gotoxy(5, 0);
        cout("\t PostgreSQL Server Connection (Postgres)");

        gotoxy(6, 0);
        cout("\t Seçiminiz:");
        return (String) reader.readLine(" ");
    }

    @Override
    public boolean menu_close() {
        clearConsole();
        generateBorders(5, 50);
        cout("\t Programdan Çıkmak İstiyor Musunuz? (E/H)");
        return reader.readLine().toLowerCase().equals("e");
    }

    @Override
    public void menu_status(String status) {
        clearConsole();
        generateBorders(5, 65);
        gotoxy(2, 0);
        switch(status){
            case "SELF_CHECK": //* Self Check
                cout("\t Sistem Kontrol Ediliyor...");
                break;
            case "IDLE": //* Idle
                cout("\t Sistem Boşta.");
                break;
            case "CONNECTING": //* Connecting
                cout("\t Sistem Bağlanıyor...");
                break;
        }
    }

    @Override
    public void menu_error(String status) {
        clearConsole();
        generateBorders(5, 65);
        gotoxy(2, 0);
        switch(status){
            case "CONNECTION_FAILED": //* Connection Failed
                cout("\t Bağlantı Başarısız!");
                break;
            case "SELF_CHECK_FAILED": //* Self Check Failed
                cout("\t Sistem Kontrolü Başarısız!");
                break;
            case "MALFUNCTION": //* Malfunction Detected
                cout("\t Sistem Hatası Algılandı!");
                break;
            default: //* Unknown Error has occurred
                cout("\t Bilinmeyen Hata!");
                break;
        }
    }

    @Override
    public double menu_newTarget(double oldTarget_temp) {
        clearConsole();
        generateBorders(6, 65);
        gotoxy(2, 0);
        cout("\t Eski Hedef Sıcaklık: " + newDoubleType.format(oldTarget_temp) + " °C");
        gotoxy(3, 0);
        cout("\t Yeni Hedef Sıcaklık: ");

        try {
            return Double.parseDouble(reader.readLine("").replaceAll(",", "\\."));
        } catch (Exception e) {
            return oldTarget_temp;
        }

    }

    @Override
    public void close() {
        try {
            terminal.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int menu_getMainSelection() {
        return main_menu_oldSelection;
    }

    
}
