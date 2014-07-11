#!/usr/bin/python

import sys
import os
import subprocess
import time
import datetime
import shutil

class BenchmarkEntry(object):
	name = ""
	
	c_dir = ""
	c_exec = ""
	c_exec_time = 0.0
	c_return_value = 0
	
	uvm_dir = ""
	uvm_exec = ""
	uvm_exec_time = 0.0
	uvm_return_value = 0
	
	def pretty_print(self):
		return "Benchmark: " + self.name + "\n" + \
				" C: " + self.c_exec + " in " + self.c_dir + "\n" + \
				" execute time: " + str(self.c_exec_time) + ", returns " + str(self.c_return_value) + "\n" + \
				" UVM: " + self.uvm_exec + " in " + self.uvm_dir + "\n" + \
				" execute time: " + str(self.uvm_exec_time) + ", returns " + str(self.uvm_return_value) + "\n"

def get_immediate_subdirs(dir) :
        return [name for name in os.listdir(dir)
	        if os.path.isdir(os.path.join(dir,name))]

def get_bm_source_path_from_dir(dir):
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
	return time.strftime("%Y%m%d_%H%M%S", time.localtime())

def compile_native(dir, dst_dir, output):
	# get all c files
	files = os.listdir(dir)
	c_files = []
	for filename in files:
		if filename.endswith(".c") :
			c_files.append(filename)
	
	# compile those c files
	os.mkdir(dst_dir)

	current_dir = os.getcwd()
	os.chdir(dir)
	rv = subprocess.call([CC] + CC_FLAGS + c_files + ['-o', os.path.join(dst_dir, output)])
	os.chdir(current_dir)
	
	if rv == 0:
		print "compile success"
	else:
		print "compile filed"
		sys.exit(1)
		
	print
	
	return rv

def start_benchmark(dir):
	return 0

uvm_root      = os.path.abspath("../../")
uvm_test_dir  = os.path.join(uvm_root, "tests/")
uvm_build_dir = os.path.join(uvm_root, "build/")
uvm_bm_dir    = os.path.join(uvm_test_dir, "micro-bm/")
uvm_bm_run_dir = os.path.join(uvm_root, "bm_run/")

CC = "gcc"
CC_FLAGS = ['-O3','-std=c99']

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

tests = set()
print

if os.path.exists(uvm_bm_run_dir):
	print "Deleting old benchmark execution dir..."
	shutil.rmtree(uvm_bm_run_dir)

print "Creating benchmark execution dir..." + uvm_bm_run_dir
os.mkdir(uvm_bm_run_dir)

print

for bm in get_immediate_subdirs(uvm_bm_dir):
	entry = BenchmarkEntry()

	time_sig = get_time_signature()
	sig = bm + "_" + time_sig
	print sig
	
	entry.name = bm

	entry.c_dir = os.path.join(uvm_bm_run_dir, bm)
	entry.c_exec = sig

	# compile native code
	compile_native( \
		os.path.join(uvm_bm_dir, bm), 
		entry.c_dir,  
		sig)

	# execute native code

	# compile uvm IR

	tests.add(entry)
	
for entry in tests:
	print entry.pretty_print()