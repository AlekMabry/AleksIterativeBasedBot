# AleksIterativeBasedBot
This is the original iterative version of the code for the roboRIO that aligns with a line on the ground in openCV. The 'translated' command-based version of this code is available in my 'AleksCommandBasedBot' repo.

The teleop consists of some tankDrive code, and a few buttons that can control my single attached pneumatic tube (used for kicking a soccer ball in the off-season) and controlling a servo that shoots a nerf dart.

The autonomous waits for a UDP message from the beaglebone containing the amount of degrees the robot should turn, and then turns to that exact position using a SPI gryo plugged into the roboRIO's SPI port. The UDP finally returns a message to the beaglebone (currently just for degbugging, but will be used to request different openCV info from the beaglebone in the future). 
