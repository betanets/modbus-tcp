/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.betanet.modbus.tcp;

import java.net.InetAddress;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.net.TCPMasterConnection;

/**
 *
 * @author Alexander Shkirkov
 */
public class ModbusTCPMain {
    /* The important instances of the classes mentioned before */
    private static TCPMasterConnection con = null; //the connection
    private static ModbusTCPTransaction trans = null; //the transaction
    private static ReadInputDiscretesRequest request = null; //the request
    private static ReadInputDiscretesResponse response = null; //the response

    public static void main(String[] args) throws Exception {
        //2. Open the connection
        con = new TCPMasterConnection(InetAddress.getLoopbackAddress()); //127.0.0.1
        con.setPort(Modbus.DEFAULT_PORT); //502
        con.connect();

        //3. Prepare the request
        int ref = 0; //the reference; offset where to start reading from
        int count = 2; //the number of DI's to read
        request = new ReadInputDiscretesRequest(ref, count);

        //4. Prepare the transaction
        trans = new ModbusTCPTransaction(con);
        trans.setRequest(request);
        
        //5. Execute the transaction repeat times
        int k = 0;
        int repeat = 1; //a loop for repeating the transaction
        
        do {
            trans.execute();
            response = (ReadInputDiscretesResponse) trans.getResponse();
            System.out.println("Digital Inputs Status=" + response.getDiscretes().toString());
            k++;
        } while (k < repeat);

        //6. Close the connection
        con.close();
    }
}
