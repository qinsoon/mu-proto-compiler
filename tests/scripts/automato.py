#!/usr/bin/python

import sys
import os
import subprocess
import time
from datetime import datetime
import shutil
import inspect
import xml.etree.cElementTree as ET

class BenchmarkEntry(object):
	"""
	This class is used to describe a single run of a benchmark
	"""
	name = ""
	run_id = 0
	dir = ""
	exe = ""
	timestamp = 0
	exec_time = 0.0
	return_value = 0
	
	def pretty_print(self):
		return "Benchmark: " + self.name + "\n" + \
				" run ID: " + str(self.run_id) + ", returns " + str(self.return_value) + "\n" + \
				" execute time: " + str(self.exec_time) + "\n"
				
class BenchmarkResults(object):
	"""
	This class is used to describe a set of runs of a benchmark
	"""
	name = ""
	result_entry_list1 = []
	result_entry_list2 = []
	
	def pretty_print(self):
		ret = "============= " + self.name + " =============" + "\n"
		ret += "Result Set 1: \n" 
		for entry in self.result_entry_list1:
			ret += entry.pretty_print()
		ret += "\n"
		ret += "Result Set 2: \n"
		for entry in self.result_entry_list2:
			ret += entry.pretty_print()
		return ret

	def report(self):
		# collect info in list 1
		print "============= Report ============="
		avg1 = report_on_list(self.result_entry_list1)
		print
		avg2 = report_on_list(self.result_entry_list2)
		print
		
		ratio = avg2 / avg1
		print "Execution time on set2 is " + '{:.2%}'.format(ratio) + " of set1"
		
		if ratio >= BEARABLE_PERFORMANCE:
			print "Performance exceeds bearable threshold (" + str(BEARABLE_PERFORMANCE) + "), failed"
			sys.exit(1)
		
		correct_rv = self.result_entry_list1[0].return_value
		for entry in self.result_entry_list2:
			if entry.return_value != correct_rv:
				print "Incorrect return value:"
				print entry.pretty_print()
				sys.exit(1)
		
		print
		
		# generate JMeter Text Log (.jtl)
		root = ET.Element("testResults")
		root.set("version", "1.2")
		
		for entry in self.result_entry_list2:
			sample = ET.SubElement(root, "sample")
			sample.set("lb", entry.name)
			sample.set("tn", entry.exe)
			sample.set("t", str(int(entry.exec_time)))
			sample.set("rc", "200")
			sample.set("rm", "OK")
			sample.set("by", "1024")
			sample.set("dt", "text")
			sample.set("ts", str(entry.timestamp))
			if entry.return_value == correct_rv:
				sample.set("s", "true")
			else:
				sample.set("s", "false")
		
		tree = ET.ElementTree(root)
		jtl_file = os.path.join(uvm_bm_run_dir, self.name + ".jtl")
		tree.write(jtl_file, encoding='utf-8', xml_declaration=True)
		
		print "Output to " + jtl_file
		print
		
def report_on_list(list):
	same_rv = True
	last_rv = None
	sum = 0.0
	for entry in list:
		sum += entry.exec_time
		if last_rv == None:
			last_rv = entry.return_value
		else:
			if last_rv != entry.return_value:
				same_rv = False
			last_rv = entry.return_value
	avg = sum / len(list)
	if same_rv:
		print "Return Value: " + str(last_rv)
		print "Avg Execution Time: " + str(avg)
	else:
		print "Error on execution: different return values"
	
	return avg
		
def get_immediate_subdirs(dir) :
	"""
	Get subdirectories of a directory (not recursively)
	"""
	return [name for name in os.listdir(dir)
		if os.path.isdir(os.path.join(dir,name))]

def get_bm_source_path_from_dir(dir):
	"""
	Get the uIR source file from a directory. Print error if there are more than one .uir files
	"""
	files = os.listdir(dir)
	ret = None
	for filename in files:
		if filename.endswith(".uir"):
			if ret is None:
				return os.path.join(dir, filename)
			else:
				print "More than one .uir sources in " + dir
				sys.exit(1)

def get_time_signature():
	"""
	Returns a time signature
	"""
	return time.strftime("%Y%m%d_%H%M%S", time.localtime())

def get_file_list(dir, extension):
	"""
	Return a list of files with the given extension
	"""
	files = os.listdir(dir)
	ret = []
	for filename in files:
		if filename.endswith(extension):
			ret.append(filename)
	return ret
	

def compile_c(dir, dst_dir, output):
	"""
	Compile C files into native code
	"""
	# get all c files
	c_files = get_file_list(dir, ".c")
	
	# compile those c files
	os.mkdir(dst_dir)

	current_dir = os.getcwd()
	os.chdir(dir)
	rv = subprocess.call([CC] + CC_FLAGS + c_files + ['-o', os.path.join(dst_dir, output)])
	os.chdir(current_dir)
	
	if rv == 0:
		print "compile success"
	else:
		print "compile failed"
		sys.exit(1)
	
	return rv
	
def compile_uir(source, dir, output):
	"""
	Compile uIR into native code
	"""
	
	# invoke uvm compiler to generate assembly
	os.mkdir(dir)
	
	current_dir = os.getcwd()
	os.chdir(dir)
	with open(os.devnull, 'w') as devnull:
		rv = subprocess.call(['java', \
			'-cp', uvm_build_dir + ":" + antlr_jar, \
			'compiler.UVMCompiler', \
			source, \
			'-base', uvm_root], stdout=devnull)
	os.chdir(current_dir)
	
	if rv == 0:
		print "invoke uvm compiler: success"
	else:
		print "invoke uvm compiler: fail"
		sys.exit(1)
	
	# invoke gcc to compile assembly
	emit_dir = os.path.join(dir, "emit/")
	assembly = os.listdir(emit_dir)
	assembly_abs_path = []
	for a in assembly:
		assembly_abs_path.append(os.path.join(emit_dir, a))
	rv = subprocess.call(['gcc'] + assembly_abs_path + ['-o', os.path.join(dir, output)])
	
	if rv == 0:
		print "assembler: success"
	else:
		print "assembler: fail"
		sys.exit(1)
	
	
def execute(entry, exe):
	"""
	Execute executable file
	"""
	start = datetime.now()
	
	rv = subprocess.call(exe)
	
	end = datetime.now()
	
	delta = end - start
	delta_millis = (delta.days * 24 * 60 * 60 + delta.seconds) * 1000 + delta.microseconds / 1000.0
	
	entry.return_value = rv
	entry.exec_time = delta_millis
	

def start_benchmark(dir):
	return 0

current_milli_time = lambda: int(round(time.time() * 1000))

script_dir    = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))

print "script_dir = " + script_dir

uvm_root      = os.path.abspath(os.path.join(script_dir, "../../"))
uvm_test_dir  = os.path.join(uvm_root, "tests/")
uvm_build_dir = os.path.join(uvm_root, "build/")
uvm_bm_dir    = os.path.join(uvm_test_dir, "micro-bm/")
uvm_bm_run_dir = os.path.join(uvm_root, "bm_run/")
antlr_jar     = os.path.join(uvm_root, "antlr-4.2-complete.jar")

CC = "gcc"
CC_FLAGS = ['-O3','-std=c99']

C_EXEC_TIMES  = 5
UIR_EXEC_TIMES = 5

if len(sys.argv) != 0:
	UIR_EXEC_TIMES = int(sys.argv[1])
	print "User defined UVM BM invocations = " + str(UIR_EXEC_TIMES)

BEARABLE_PERFORMANCE = 10

print "===== uVM compiler automated tests ====="

build_done = os.path.exists(uvm_build_dir)

print "\nCheck if uvm compiler build dir \"" + uvm_build_dir + "\" exists: "
print "......" + str(build_done)

if not build_done:
	print "Need to build uvm compiler..."
	current_dir = os.getcwd()
	os.chdir(uvm_root)
	rv = subprocess.call(['ant', 'build'])
	if rv != 0:
		print "Error on building uvm compiler"
		sys.exit(1)
	else:
		print "Build done"
	os.chdir(current_dir)

print "\n--- Starting tests ---"

print

if os.path.exists(uvm_bm_run_dir):
	print "Deleting old benchmark execution dir..."
	shutil.rmtree(uvm_bm_run_dir)

print "Creating benchmark execution dir..." + uvm_bm_run_dir
os.mkdir(uvm_bm_run_dir)

print

id = 0

for bm in get_immediate_subdirs(uvm_bm_dir):
	bm_result = BenchmarkResults()
	
	bm_result.name = bm

	# run C benchmakr for C_EXEC_TIMES
	c_result_list = []
	
	for n in xrange(C_EXEC_TIMES):
		c_result = BenchmarkEntry()
			
		c_result.run_id = id
		id += 1
		bm_with_id = str(c_result.run_id) + "_" + bm
	
		time_sig = get_time_signature()
		sig = bm_with_id + "_" + time_sig
		print sig

		c_result.name = bm	
		c_result.dir = os.path.join(uvm_bm_run_dir, bm_with_id)
		c_result.exe = sig
		c_result.timestamp = current_milli_time()
	
		# compile native code
		compile_c( \
			os.path.join(uvm_bm_dir, bm), 
			c_result.dir,  
			sig)
	
		# execute native code
		execute(c_result, os.path.join(c_result.dir, sig))
		
		# clear execution dir
		# shutil.rmtree(c_result.dir)
		
		print c_result.pretty_print()
		print 
		c_result_list.append(c_result)

	bm_result.result_entry_list1 = c_result_list
	
	# run uvm benchmark for UIR_EXEC_TIMES
	uir_result_list = []
	
	for n in xrange(UIR_EXEC_TIMES):
		uir_result = BenchmarkEntry()
		
		uir_result.run_id = id
		id += 1
		bm_with_id = str(uir_result.run_id) + "_" + bm
				
		time_sig = get_time_signature()
		sig = bm_with_id + "_" + time_sig
		print sig		

		uir_result.name = bm		
		uir_result.dir = os.path.join(uvm_bm_run_dir, bm_with_id)
		uir_result.exe = sig
		uir_result.timestamp = current_milli_time()
		
		# compile uir code
		uir_source = get_bm_source_path_from_dir(os.path.join(uvm_bm_dir, bm))
		compile_uir(uir_source, uir_result.dir, sig)
		
		# execute native code
		execute(uir_result, os.path.join(uir_result.dir, sig))
		
		# clear execution dir
		# shutil.rmtree(uir_result.dir)
		
		print uir_result.pretty_print()
		print
		uir_result_list.append(uir_result)
	
	bm_result.result_entry_list2 = uir_result_list
	
	print bm_result.pretty_print()
	bm_result.report()
