# pjs-sandbox

```shell
./gradlew :pjs-sandbox:pjs-examples:installDist
```

```shell
➜  pjs git:(main)         Options                              Description                   
 -p, --plugin <arg>        Which plugin to load. (`mock`, `grpc`, `ffm`,   
                            `pi4j`)                                        
 -G, --grpc-host <arg>     The gRPC host to connect to.                    
 -P, --grpc-port <arg>     The gRPC port to connect to.                    
 -m, --mode <arg>          The mode to run in. (`mock`, `grpc`, `ffm`,     
                            `pi4j`)                                        
 -l, --logging             Enable debug logging for IO operations          
 --gpio                    Run the GPIO example                            
 --i2c                     Run the I2C example                             
 --seven-segment           Run the seven-segment example                   
 --spi                     Run the SPI example                             
 --eeprom                  Run the EEPROM example                          
 --328                     Run the three-to-eight decoder example          
 --pwm                     Run the PWM example                             
 --mcp                     Run the MCP23017 example                        
 --oled                    Run the OLED example                            
 --debounce                Run the debounce example                        
 -h, --help                Display help information                        
```