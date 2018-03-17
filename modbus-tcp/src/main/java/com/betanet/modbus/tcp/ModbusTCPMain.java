/*
 * The MIT License
 *
 * Copyright 2018 Alexander Shkirkov.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.betanet.modbus.tcp;

import java.net.InetAddress;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.util.BitVector;

/**
 *
 * @author Alexander Shkirkov
 */
public class ModbusTCPMain {
    //Connection and transaction entities
    private static TCPMasterConnection connection = null;
    private static ModbusTCPTransaction transactionFC2 = null;
    private static ModbusTCPTransaction transactionFC4 = null;
    
    //Request/response for input statuses (discrete inputs) (FC02)
    private static ReadInputDiscretesRequest requestFC2 = null;
    private static ReadInputDiscretesResponse responseFC2 = null;
    
    //Request/response for analog input registers (FC04)
    private static ReadInputRegistersRequest requestFC4 = null;
    private static ReadInputRegistersResponse responseFC4 = null;
    
    public static void main(String[] args) {
        
        //Create connection
        InetAddress connectionAddress = InetAddress.getLoopbackAddress(); //127.0.0.1
        int connectionPort = Modbus.DEFAULT_PORT; //502
        try {
            connection = new TCPMasterConnection(connectionAddress);
            connection.setPort(connectionPort);
            connection.connect();
        } catch (Exception ex) {
            System.out.println(new StringBuilder("ERROR: Could not establish connection to slave device. Device address: ")
                    .append(connectionAddress.getHostAddress())
                    .append(':')
                    .append(connectionPort));
            connection.close();
            return;
        }

        //Read 16 analog input registers starting from 30001
        int offsetFC4 = 30001;
        int countFC4 = 16;
        requestFC4 = new ReadInputRegistersRequest(offsetFC4, countFC4);
        
        //Read 24 discrete inputs starting from 10001
        int offsetFC2 = 10001;
        int countFC2 = 24;
        requestFC2 = new ReadInputDiscretesRequest(offsetFC2, countFC2);
        
        //Create request transactions
        transactionFC2 = new ModbusTCPTransaction(connection);
        transactionFC4 = new ModbusTCPTransaction(connection);
        transactionFC2.setRequest(requestFC2);
        transactionFC4.setRequest(requestFC4);
        
        //Infinite read loop
        int analogAddIndex = 0;
        int requestIndex = 0;
        try {
            while(true) {
                System.out.println(new StringBuilder("---=== REQUEST ").append(requestIndex).append(" ===---"));
                transactionFC4.execute();
                responseFC4 = (ReadInputRegistersResponse)transactionFC4.getResponse();
                for (InputRegister register : responseFC4.getRegisters()) {
                    System.out.println(new StringBuilder("Analog register ")
                            .append(offsetFC4 + analogAddIndex)
                            .append(": ")
                            .append(register.getValue()));
                }

                transactionFC2.execute();
                responseFC2 = (ReadInputDiscretesResponse)transactionFC2.getResponse();
                BitVector discreteInputs = responseFC2.getDiscretes();
                for(int i = 0; i< discreteInputs.size(); i++){
                    System.out.println(new StringBuilder("Discrete input ")
                            .append(offsetFC2 + i)
                            .append(": ")
                            .append(discreteInputs.getBit(i)));
                }

                analogAddIndex = 0;
                requestIndex++;
            }
        } catch (ModbusException ex) {
            connection.close();
        }
        
        //Close connection
        connection.close();
    }
}
