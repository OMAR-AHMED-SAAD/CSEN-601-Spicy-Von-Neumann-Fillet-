# CSEN 601/Spicy-Von-Neumann-Fillet Processor Simulation
This is the final project for the Computer System Architecture CSEN 601 course.
The project is a simulation of a Von Neumann processor with a 5-stage data path (fetch, decode, execute, memory, and write back). The simulation is intended to use pipelining of the 5 stages and deal with both control hazards and data hazards.In addition to implementing a control unit to control all simulated hardware.

## Getting Started

To get started with the project, follow these steps:

1. Clone the repository to your local machine.
2. Open the project in your preferred Java IDE.
3. Access the resources directory of the project.
4. Write the instructions you want to execute from the instruction sheet in the `program.txt` file.
5. Run the `Processor.java` class to start the simulation.

## Instruction Sheet

The instruction sheet contains a list of instructions that can be executed by the processor. Each instruction is represented by a 32-bit binary code, which is divided into different fields that specify the opcode, registers, and immediate values used by the instruction.

![Instruction Sheet](https://github.com/OMAR-AHMED-SAAD/Spicy-Von-Neumann-Fillet/assets/110069095/d9df19a1-915d-479a-83b5-5e17a9f369c2)


## Data Hazards and Control Hazards

The simulation deals with both data hazards and control hazards. Data hazards occur when an instruction depends on the result of a previous instruction that has not yet completed. Control hazards occur when the processor encounters a branch instruction and needs to flush the pipeline to start executing instructions from a different location in the program.

To handle data hazards, the simulation uses forwarding technique to ensure that instructions are executed in the correct order.

## Contributors
+ [Farah Maher](https://github.com/farahalfawzy)
+ [Omar Ahmed](https://github.com/OMAR-AHMED-SAAD)
+ [Malak El Wassif](https://github.com/malakElWassif)
+ [Abdullah El Nahas](https://github.com/AbdullahElNahas)
+ [Habiba Mohamed](https://github.com/HabibaMohamedd4)
