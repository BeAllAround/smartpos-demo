package at.tecs.smartpos_demo.main;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import at.tecs.smartpos.PaymentService;
import at.tecs.smartpos.connector.ConnectionListener;
import at.tecs.smartpos.connector.ResponseListener;
import at.tecs.smartpos.data.ConnectionType;
import at.tecs.smartpos.data.Response;
import at.tecs.smartpos.data.Transaction;
import at.tecs.smartpos_demo.data.repository.Repository;
import at.tecs.smartpos_demo.data.repository.entity.HostnameEntity;
import at.tecs.smartpos_demo.data.repository.entity.PortEntity;
import at.tecs.smartpos_demo.data.repository.entity.TerminalNumberEntity;
import at.tecs.smartpos_demo.data.repository.entity.TransactionEntity;

import static at.tecs.smartpos.data.ConnectionType.BLUETOOTH;
import static at.tecs.smartpos.data.ConnectionType.TCP;


public class MainPresenter implements MainContract.Presenter {

    private MainContract.View view;
    private MainContract.View.ConnectionTab connectionView;
    private MainContract.View.TransactionTab transactionView;
    private MainContract.View.ResponseTab responseView;
    private MainContract.View.TemplatesTab templatesView;

    private Repository repository;

    private Timer timer;
    private Incrementer incrementer;

    private String transactionID;
    private String dateTime;

    private String TID;
    private String hostname;
    private String port;

    private Response lastResponse;

    private ArrayList<String> transactionNames = new ArrayList<>();
    private ArrayList<String> terminalNums = new ArrayList<>();
    private ArrayList<String> hostnames = new ArrayList<>();
    private ArrayList<String> ports = new ArrayList<>();

    private PaymentService paymentService;

    private ConnectionType connectionType;

    private BluetoothDevice bluetoothDevice;

    private BluetoothAdapter bluetoothAdapter;

    private ConnectionType selected = TCP;

    MainPresenter() {
        paymentService = new PaymentService();
        connectionType = paymentService.getType();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void takeView(MainContract.View view) {
        this.view = view;
    }

    /**
     * Starts counter.
     */
    @Override
    public void start() {
        timer.schedule(incrementer, 1000, 1000);
    }

    @Override
    public void initialize() {
        incrementer = new Incrementer();

        timer = new Timer();
        repository = new Repository(view.getContext());
    }

    /**
     * Saves transaction to database.
     * @param transaction Transaction data object.
     * @param name Name of transaction.
     */
    @Override
    public void saveTransaction(Transaction transaction, String name) {

        for(int i = 0; i < transactionNames.size(); i ++) {
            if(name.equals(transactionNames.get(i))) {
                view.showToast("Transaction with this name is already saved !");
                return;
            }
        }

        TransactionEntity trans = convertTransaction(transaction, name);

        repository.saveTransaction(trans);

        transactionNames.add(transaction.getTag());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, transactionNames);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        transactionView.setTransactionAdapter(arrayAdapter);
    }

    @Override
    public void deleteTransaction(String name) {
        repository.deleteTransation(name);

        transactionNames = repository.getTransactionsNames();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, transactionNames);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        transactionView.setTransactionAdapter(arrayAdapter);
        if(transactionNames.isEmpty()) {
            transactionView.showTransaction(new TransactionEntity());
        } else {
            transactionView.showTransaction(repository.getTransaction(transactionNames.get(0)));
        }
    }

    /**
     * Saves terminal number to database.
     * @param terminalNum Terminal number.
     */
    @Override
    public void saveTermNum(String terminalNum) {

        for(int i = 0; i < terminalNums.size(); i ++) {
            if(terminalNum.equals(terminalNums.get(i))) {
                view.showToast("Terminal number is already saved !");
                return;
            }
        }

        repository.saveTerminalNum(new TerminalNumberEntity(terminalNum));

        terminalNums.add(terminalNum);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, terminalNums);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        connectionView.setTerminalNumAdapter(arrayAdapter);
    }

    @Override
    public void deleteTermNum(String terminalNum) {
        repository.deleteTerminalNum(new TerminalNumberEntity(terminalNum));

        terminalNums = repository.getTerminalNumbers();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, terminalNums);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        connectionView.setTerminalNumAdapter(arrayAdapter);
    }

    /**
     * Saves hostname to database.
     * @param hostname Hostname address.
     */
    @Override
    public void saveHostName(String hostname) {

        for(int i = 0; i < hostnames.size(); i ++) {
            if(hostname.equals(hostnames.get(i))) {
                view.showToast("Hostname is already saved !");
                return;
            }
        }

        repository.saveHostname(new HostnameEntity(hostname));

        hostnames.add(hostname);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, hostnames);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        connectionView.setHostnameAdapter(arrayAdapter);
    }

    @Override
    public void deleteHostName(String hostname) {
        repository.deleteHostname(new HostnameEntity(hostname));

        hostnames = repository.getHostnames();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, hostnames);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        connectionView.setHostnameAdapter(arrayAdapter);
    }

    /**
     * Saves port to database.
     * @param port Port number.
     */
    @Override
    public void savePort(String port) {

        for(int i = 0; i < ports.size(); i ++) {
            if(port.equals(ports.get(i))) {
                view.showToast("Port is already saved !");
                return;
            }
        }

        repository.savePort(new PortEntity(port));

        ports.add(port);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, ports);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        connectionView.setPortAdapter(arrayAdapter);
    }

    @Override
    public void deletePort(String port) {
        repository.deletePort(new PortEntity(port));

        ports = repository.getPorts();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, ports);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        connectionView.setPortAdapter(arrayAdapter);
    }

    /**
     * Starts or terminates automatic incrementation of parameters.
     * @param automatic Automatic incrementation.
     */
    @Override
    public void startAutomatic(boolean automatic) {
        if (automatic) {
            incrementer.go();
        } else {
            incrementer.terminate();
        }
    }

    /**
     * Connects to hostname:port.
     */
    @Override
    public void connect() {
        if(connectionView.checkConnectionInputs())
            paymentService.connect(new ConnectionListener() {
                @Override
                public void onConnected() {
                    view.showConnected();

                    view.showToast("Connected to " + paymentService.getHostname() + ":" + paymentService.getPort());

                    paymentService.listen(new ResponseListener() {
                        @Override
                        public void onResponseReceived(Response response) {     //Readed response
                            lastResponse = response;
                            view.showResponseTab(Integer.valueOf(response.responseCode));
                            if(responseView != null) {
                                responseView.showResponse(response);
                            }
                        }

                        @Override
                        public void onReadFailed() {
                            view.showToast("Read Failed !");
                        }
                    });
                }

                @Override
                public void onUnknownHost(UnknownHostException e) {
                    e.printStackTrace();

                    view.showToast("Unknown Host !");
                    view.showDisconnected();
                }

                @Override
                public void onSocketFail(IOException e) {
                    e.printStackTrace();

                    view.showToast("Socket Failed !");
                    view.showDisconnected();
                }
            });
    }

    /**
     * Disconnect from hostname:port
     */
    @Override
    public void disconnect() {
        try {
            paymentService.disconnect();

            bluetoothDevice = null;

            view.showToast("Disconnected from " + paymentService.getHostname() + ":" + paymentService.getPort());
            view.showDisconnected();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Send transaction.
     */
    @Override
    public void send() {
        if(connectionView.checkConnectionInputs() && transactionView.checkTransactionInputs()) {
            Transaction transaction = transactionView.createTransaction();
            transaction.terminalNum = TID;

            TransactionEntity lastTransaction = repository.getTransaction("Last Transaction");

            if(lastTransaction == null) {
                repository.saveTransaction(convertTransaction(transaction, "Last Transaction"));
            } else {
                repository.deleteTransation("Last Transaction");
                repository.saveTransaction(convertTransaction(transaction, "Last Transaction"));
            }

            initializeTransactionSpinners();

            paymentService.sendTransaction(transaction);

            view.showToast("Message has been send to " + paymentService.getHostname() + ":" + paymentService.getPort());
        }
    }

    @Override
    public void selectConnection(String type) {
        if(type.equals("Bluetooth") && paymentService.getType() != BLUETOOTH ) {
            try {
                view.showToast("Changed to bluetooth");

                paymentService.disconnect();
                view.showDisconnected();

                paymentService = new PaymentService(BLUETOOTH);
                paymentService.setDevice(bluetoothDevice);

                connectionView.showBluetooth();

                connectionType = BLUETOOTH;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (type.equals("TCP") && paymentService.getType() != TCP) {
            try {
                view.showToast("Changed to TCP");

                paymentService.disconnect();
                view.showDisconnected();

                paymentService = new PaymentService(TCP);
                paymentService.setHostname(hostname);
                paymentService.setPort(Integer.valueOf(port));

                connectionView.showTCP();

                connectionType = TCP;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void sale(String amount, String currency) {
        paymentService.sale(Integer.valueOf(TID), Integer.valueOf(amount), currency);
    }

    @Override
    public void refund(String amount, String currecy) {
        paymentService.refund(Integer.valueOf(TID), Integer.valueOf(amount), currecy);
    }

    @Override
    public void cancellation(String transID, String amount, String currency) {
        paymentService.cancellation(Integer.valueOf(TID), transID,  Integer.valueOf(amount), currency);
    }

    @Override
    public void abort() {
        paymentService.abort();
    }

    /**
     * Load transaction from database.
     * @param name Name of saved transaction.
     */
    @Override
    public void loadTransaction(String name) {
        TransactionEntity transactionEntity = repository.getTransaction(name);

        transactionView.showTransaction(transactionEntity);
    }

    @Override
    public void setHostname(String hostname) {
        paymentService.setHostname(hostname);

        this.hostname = hostname;
    }

    @Override
    public void setPort(String port) {
        paymentService.setPort(Integer.valueOf(port));

        this.port = port;
    }

    @Override
    public void setTID(String TID) {
        this.TID = TID;
    }

    @Override
    public boolean isConnected() {
        return paymentService.isConnected();
    }

    @Override
    public Response getLastResponse() {
        return this.lastResponse;
    }

    @Override
    public int startNatali(Context context) {
       return paymentService.startService(context);
    }

    @Override
    public PaymentService getPaymentService() {
        return paymentService;
    }

    @Override
    public void takeConnectionView(MainContract.View.ConnectionTab view) {
        connectionView = view;

        initializeConnectionSpinners();
    }

    @Override
    public void takeTransactionView(MainContract.View.TransactionTab view) {
        transactionView = view;

        initializeTransactionSpinners();
    }

    @Override
    public void takeResponseView(MainContract.View.ResponseTab view) {
        responseView = view;

        if(lastResponse != null) {
            responseView.showResponse(lastResponse);
        }
    }

    @Override
    public void takeTemplatesView(MainContract.View.TemplatesTab view) {
        templatesView = view;
    }

    @Override
    public void startScan() {
        if(bluetoothAdapter.startDiscovery()) {
            Log.d("DBG","startScan run !");
        } else {
            Log.d("DBG","startScan failed !");
        }
    }

    @Override
    public void stopScan() {
        if(bluetoothAdapter.cancelDiscovery()) {
            Log.d("DBG","stopScan run !");
        } else {
            Log.d("DBG","stopScan failed !");
        }
    }

    @Override
    public Set<BluetoothDevice> getPairedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }

    @Override
    public ConnectionType getSelected() {
        return selected;
    }

    private class Incrementer extends TimerTask {

        private volatile boolean alive;

        Incrementer() {
            alive = true;
        }

        void terminate() {
            alive = false;
        }

        void go() {
            alive = true;
        }

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        @Override
        public void run() {
            if (alive) {
                Date date = new Date(System.currentTimeMillis());
                transactionID = formatter.format(date) + "000000";
                dateTime = formatter.format(date);
                transactionView.showTransactionAuto(transactionID, dateTime);
            }
        }
    }

    @Override
    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        if(bluetoothDevice != null) {
            paymentService.setDevice(bluetoothDevice);
        }
    }

    private void initializeConnectionSpinners() {

        terminalNums = repository.getTerminalNumbers();

        if (!terminalNums.isEmpty()) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, terminalNums);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            connectionView.setTerminalNumAdapter(arrayAdapter);
        }

        hostnames = repository.getHostnames();

        if (!hostnames.isEmpty()) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, hostnames);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            connectionView.setHostnameAdapter(arrayAdapter);
        }

        ports = repository.getPorts();

        if (!ports.isEmpty()) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, ports);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            connectionView.setPortAdapter(arrayAdapter);
        }
    }

    private void initializeTransactionSpinners() {
        transactionNames = repository.getTransactionsNames();

        if (!transactionNames.isEmpty()) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, transactionNames);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            transactionView.setTransactionAdapter(arrayAdapter);
            transactionView.showTransaction(repository.getTransaction(transactionNames.get(0)));
        }
    }

    private TransactionEntity convertTransaction(Transaction transaction,String name) {
        TransactionEntity trans = new TransactionEntity();

        trans.name = name;
        trans.ID = transaction.ID;
        trans.msgType = transaction.msgType;
        trans.dateTime = transaction.dateTime;
        trans.sourceID = transaction.sourceID;
        trans.cardNum = transaction.cardNum;
        trans.cvc2 = transaction.cvc2;
        trans.amount = transaction.amount;
        trans.currency = transaction.currency;
        trans.terminalNum = TID;
        trans.receiptNum = transaction.receiptNum;
        trans.transPlace = transaction.transPlace;
        trans.authorNum = transaction.authorNum;
        trans.originInd = transaction.originInd;
        trans.password = transaction.password;
        trans.userdata = transaction.userdata;
        trans.langCode = transaction.langCode;
        trans.desCurrency = transaction.desCurrency;
        trans.receiptLayout = transaction.receiptLayout;
        trans.txOrigin = transaction.txOrigin;
        trans.personalID = transaction.personalID;

        return trans;
    }
}
