package compiler.phase.mc.x64;

import java.util.List;
import java.util.ArrayList;

import burm.mc.X64add;
import burm.mc.X64pop;
import burm.mc.X64push;
import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;
import uvm.inst.InstCall;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;

public class X64UVMCallConvention extends X64CDefaultCallConvention {
}