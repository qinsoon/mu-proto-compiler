package uvm.inst;

public class InstInternalPrintStr extends AbstractInternalInstruction {
	String literal;
	
	public InstInternalPrintStr(String literal) {
		this.literal = literal;
	}
	
	@Override
	public String prettyPrint() {
		return "PRINTSTR " + literal;
	}
	
	long[] int64array;
	
	public long[] stringLiteralToInt64() {
		if (int64array != null)
			return int64array;
		
		String[] hex = new String[literal.length() + 1];
		for (int i = 0; i < literal.length(); i++) {
			char c = literal.charAt(i);
			hex[i] = Integer.toHexString(c);
		}
		hex[hex.length - 1] = "00";
		
		long[] ret;
		if (hex.length % 8 == 0)
			ret = new long[hex.length / 8];
		else ret = new long[hex.length / 8 + 1];
		
		for (int i = 0, ii = 0; i < hex.length; i+= 8, ii++) {
			StringBuilder s = new StringBuilder();
			for (int j = 7; j >= 0; j--) {
				if (i + j > hex.length - 1)
					s.append("00");
				else s.append(hex[i+j]);
			}
			long l = Long.valueOf(s.toString(), 16);
			ret[ii] = l;
		}
		
		return ret;
	}

	@Override
	public boolean needsToExpandIntoRuntimeCall() {
		return true;
	}
}
