# BrailleBandCommunicator
communication interface library for the product BrailleBand

# BrailleBand
BrailleBand is a wearable device which can translate data into braille language and transfer them to the user using vibration patterns. It is designed for the visually impaired people to interact with electronic devices. An electronic device can connect to the BrailleBand through Bluetooth and transfer data to it. 

# brailleband-communicator library
This library is designed to connect android smart phone with the BrailleBand. The library provide basic functionalities for an android application to interact with the BraillBand.

# Using brailleband-communicator

To use the brailleband-communicator, include the following in your module's build.gradle file:

```gradle
compile 'com.example.hareenudayanath.braillebandcommunicator:braillebandcommunicator:1.0.0'
```
You also can use maven dependency 
```maven
<dependency>
  <groupId>com.example.hareenudayanath.braillebandcommunicator</groupId>
  <artifactId>braillebandcommunicator</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

# Example

Create the BrailleBandCommunicator instance 

```java
BrailleBandCommunicator brailleBandCommunicator = new BrailleBandCommunicator();
```

Connect to the BrailleBand using the MAC address

```java
// address -- MAC address of the BrailleBand
if(!brailleBandCommunicator.connect(address)) {
    showToastMessage("Cannot connect to the device");           
}
```

Disconnect

```java
brailleBandCommunicator.disConnect();
```
Send Character to the BrailleBand

```java
brailleBandCommunicator.write(chr);
```


