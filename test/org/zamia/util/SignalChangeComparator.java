package org.zamia.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example input:
 * <p/>
 * : DRIVER@590393[SVSFR_DEBUG_MUX_OUT]=00000000<=00000000"
 * : DRIVER@764635[DOUTA]=>DRIVER@764252[DAT_OUT]=>DRIVER@560626[SVCPU_STACKCONT_DATA_IN]=0000000000000000<=0000000000000000"
 *
 * @author Anton Chepurov
 */
public class SignalChangeComparator {

	private final static Pattern NAME = Pattern.compile("@\\d+\\[(\\w+)\\]");
	private final static Pattern VALUES = Pattern.compile("(\\w+?<=\\w+)");

	private final Map<String, List<SignalChange>> signalChanges = new HashMap<String, List<SignalChange>>();

	private final List<Pair<List<SignalChange>, List<SignalChange>>> missing = new LinkedList<Pair<List<SignalChange>, List<SignalChange>>>();
	private final List<Pair<List<SignalChange>, List<SignalChange>>> different = new LinkedList<Pair<List<SignalChange>, List<SignalChange>>>();

	private class SignalChange {

		List<String> names = new LinkedList<String>();

		String firstName, valueOld, valueNew;

		String description;

		public SignalChange(String signalChange) {

			Matcher namesMatcher = NAME.matcher(signalChange);
			while (namesMatcher.find()) {
				String name = namesMatcher.group();
				String[] parts = name.split("[\\[\\]@]");
				names.add(parts[2] + "(" + parts[1] + ")");
			}

			Matcher valMatcher = VALUES.matcher(signalChange);
			if (valMatcher.find()) {
				String[] values = valMatcher.group().split("<=");
				valueOld = values[0].trim();
				valueNew = values[1].trim();
			}
		}

		public String getFirstName() {
			if (firstName == null) {
				if (names.size() > 0) {
					String name = names.get(0);
					firstName = name.substring(0, name.indexOf("("));
				} else {
					throw new RuntimeException("SignalChange without a name, with values: " + valueOld + " <= " + valueNew);
				}
			}
			return firstName;
		}

		@Override
		public String toString() {
			if (description == null) {
				StringBuilder builder = new StringBuilder();
				for (String name : names) {
					if (builder.length() > 0) {
						builder.append("==>");
					}
					builder.append(name);
				}
				builder.append("=").append(valueOld).append("<=").append(valueNew);
				description = builder.toString();
			}
			return description;
		}
	}

	public SignalChangeComparator(String... signalChanges) {
		for (String signalChange : signalChanges) {
			SignalChange sc = new SignalChange(signalChange);
			List<SignalChange> changes = this.signalChanges.get(sc.getFirstName());
			if (changes == null) {
				changes = new LinkedList<SignalChange>();
				this.signalChanges.put(sc.getFirstName(), changes);
			}
			changes.add(sc);
		}
	}


	private void apply(SignalChangeComparator second) {

		Map<String, List<SignalChange>> secMap = second.signalChanges;

		for (Map.Entry<String, List<SignalChange>> entry : signalChanges.entrySet()) {

			String name = entry.getKey();
			List<SignalChange> changes = entry.getValue();

			if (!secMap.containsKey(name)) {
				missing.add(new Pair<List<SignalChange>, List<SignalChange>>(changes, null));
				continue;
			}

			List<SignalChange> secChanges = secMap.get(name);
			List<SignalChange> notFound = new LinkedList<SignalChange>();
			for (SignalChange change : changes) {
				boolean found = false;
				for (SignalChange secChange : secChanges) {
					if (secChange.valueOld.equals(change.valueOld) && secChange.valueNew.equals(change.valueNew)) {
						found = true;
						break;
					}
				}
				if (!found) {
					notFound.add(change);
				}
			}

			if (!notFound.isEmpty()) {
				different.add(new Pair<List<SignalChange>, List<SignalChange>>(notFound, secChanges));
			}

		}
	}

	public static void main(String[] args) {

		SignalChangeComparator first = new SignalChangeComparator(delta_1);
		SignalChangeComparator second = new SignalChangeComparator(delta_1_bug);

		System.out.println(" <<<<<<<<<<<<<<<   CORRECT vs BUGGY >>>>>>>>>>>>>");
		first.apply(second);
		first.report();

		System.out.println(" <<<<<<<<<<<<<<<   BUGGY vs CORRECT >>>>>>>>>>>>>");
		second.apply(first);
		second.report();

	}

	private void report() {
		if (!different.isEmpty()) {
			System.out.println("##### " + different.size() + " sets of Different transitions: ");
			for (Pair<List<SignalChange>, List<SignalChange>> entry : different) {
				for (SignalChange ch : entry.getFirst()) {
					System.out.println(ch);
				}
				System.out.println("... not found in here:");
				for (SignalChange ch : entry.getSecond()) {
					System.out.println(ch);
				}
				System.out.println();
			}
		}
		if (!missing.isEmpty()) {
			System.out.println("##### Missing transitions: ");
			for (Pair<List<SignalChange>, List<SignalChange>> entry : missing) {
				for (SignalChange ch : entry.getFirst()) {
					System.out.println(ch);
				}
				System.out.println();
			}
		}
	}


	static String[] delta_1 = {": DRIVER@807288[SVDATA_WRITE]=00001011<=01110011\"",
			": DRIVER@590205[SVSTATUS_REG]=0100<=0100\"",
			": DRIVER@807306[SVDATA_READ]=01110111<=01110111\"",
			": DRIVER@582763[SSSTATE_EXEC]=NORMAL_EXEC<=NORMAL_EXEC\"",
			": DRIVER@807326[SVRETRY_COUNT]=000<=000\"",
			": DRIVER@807334[SVWAIT_ACK_COUNT]=000<=000\"",
			": DRIVER@764519[DOUTA]=>DRIVER@764136[DAT_OUT]=>DRIVER@560510[SVCPU_STACKCONT_DATA_IN]=0000000000000000<=0000000000000000\"",
			": DRIVER@582793[SVINT_REG]=00000000<=00000000\"",
			": DRIVER@844650[SFACK_REG]=0<=0\"",
			": DRIVER@576629[SVINSTRUCTION]=000100000100111001<=000100000100111001\"",
			": DRIVER@582811[SVINTERRUPT_ACTIVE]=000<=000\"",
			": DRIVER@576835[SVOP_MUX]=00001011<=11111110\"",
			": DRIVER@583460[SSSTATE_EXEC]=NORMAL_EXEC<=NORMAL_EXEC\"",
			": DRIVER@581201[SVPROG_COUNTER]=000100010000<=000100010000\"",
			": DRIVER@581227[SVPREV_ADDRESS]=000100001111<=000100001111\"",
			": DRIVER@581253[SVNEXT_ADDRESS]=000100010000<=000100010000\"",
			": DRIVER@592053[DOUTA]=>DRIVER@560441[SVCPU_PROGMEM_DATA_IN]=000100000100111001<=100101000010000111\"",
			": DRIVER@590277[SVSFR_DEBUG_MUX_OUT]=00000000<=00000000\"",
			": DRIVER@590251[SVEXC_CODE]=000<=000\"",
			": DRIVER@584181[SAVREG]=00001000000110010111111111111110010101001111100110101001000010110001001001110011000101010001010100011011000000010000000000000000<=000010000001100101111111111111100101010011111001101010010000101100010010011100110001010...",
			": DRIVER@590224[SVEXC_INT_MASK]=00000000<=00000000\"",
			": DRIVER@581033[SSSTATE]=INST_DECODE<=EXECUTE\"",
			": DRIVER@764211[SSSTATE]=SEMPTY<=SEMPTY\"",
			": DRIVER@764402[SVSTACK_POINTER_READ]=0000000000<=0000000000\"",
			": DRIVER@764212[SVSTACK_POINTER_WRITE]=0000000001<=0000000001\"",
			": DRIVER@583461[SFEXC_REG]=0<=0\"",
			": DRIVER@583463[SVEXCEPTION_CODE]=000<=000\"",
			": DRIVER@844613[SVREGISTER_OUT]=00000000<=00000000\"",
			": DRIVER@844631[SVREGISTER_IN]=00000000<=00000000\"",
			": DRIVER@591396[SVADDRESS]=0000100000010101<=0000100011111110\"",
			": DRIVER@807270[SSSTATE]=IDLE<=IDLE\""};

	static String[] delta_1_bug = {": DRIVER@590393[SVSFR_DEBUG_MUX_OUT]=00000000<=00000000\"",
			": DRIVER@844729[SVREGISTER_OUT]=00000000<=00000000\"",
			": DRIVER@844747[SVREGISTER_IN]=00000000<=00000000\"",
			": DRIVER@581149[SSSTATE]=INST_DECODE<=EXECUTE\"",
			": DRIVER@590340[SVEXC_INT_MASK]=00000000<=00000000\"",
			": DRIVER@764635[DOUTA]=>DRIVER@764252[DAT_OUT]=>DRIVER@560626[SVCPU_STACKCONT_DATA_IN]=0000000000000000<=0000000000000000\"",
			": DRIVER@591512[SVADDRESS]=0000100000010101<=0000100011111110\"",
			": DRIVER@807422[SVDATA_READ]=01110111<=01110111\"",
			": DRIVER@576951[SVOP_MUX]=00001011<=11111110\"",
			": DRIVER@582927[SVINTERRUPT_ACTIVE]=000<=000\"",
			": DRIVER@583576[SSSTATE_EXEC]=NORMAL_EXEC<=NORMAL_EXEC\"",
			": DRIVER@590321[SVSTATUS_REG]=X0X1<=X0X1\"",
			": DRIVER@582909[SVINT_REG]=00000000<=00000000\"",
			": DRIVER@807442[SVRETRY_COUNT]=000<=000\"",
			": DRIVER@807450[SVWAIT_ACK_COUNT]=000<=000\"",
			": DRIVER@844766[SFACK_REG]=0<=0\"",
			": DRIVER@576745[SVINSTRUCTION]=000100000100111001<=000100000100111001\"",
			": DRIVER@592169[DOUTA]=>DRIVER@560557[SVCPU_PROGMEM_DATA_IN]=000100000100111001<=100101000010000111\"",
			": DRIVER@584297[SAVREG]=00001000000110010111111111111110010101001111100110101001ZZZZ10110001001001110011000101010001010100011011000000010000000000000000<=00001000000110010111111111111110010101001111100110101001ZZZZ101100010010011100110001010...",
			": DRIVER@583577[SFEXC_REG]=0<=0\"",
			": DRIVER@583579[SVEXCEPTION_CODE]=000<=000\"",
			": DRIVER@807404[SVDATA_WRITE]=ZZZZ1011<=01110011\"",
			": DRIVER@807386[SSSTATE]=IDLE<=IDLE\"",
			": DRIVER@581317[SVPROG_COUNTER]=000100010000<=000100111001\"",
			": DRIVER@581343[SVPREV_ADDRESS]=000100001111<=000100001111\"",
			": DRIVER@581369[SVNEXT_ADDRESS]=000100010000<=000100010000\"",
			": DRIVER@582879[SSSTATE_EXEC]=NORMAL_EXEC<=NORMAL_EXEC\"",
			": DRIVER@590367[SVEXC_CODE]=000<=000\"",
			": DRIVER@764327[SSSTATE]=SEMPTY<=SEMPTY\"",
			": DRIVER@764518[SVSTACK_POINTER_READ]=0000000000<=0000000000\"",
			": DRIVER@764328[SVSTACK_POINTER_WRITE]=0000000001<=0000000001\""};

	static String[] delta_2 = {"[0] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3883}\"@ 17870000000: DRIVER@581034[SVCODED_CPU_STATE]=001<=100\"",
			"[1] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3884}\"@ 17870000000: DRIVER@581023[CPU_STATE_OUT]=>DRIVER@576221[SSSTATE]=INST_DECODE<=EXECUTE\"",
			"[2] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3885}\"@ 17870000000: DRIVER@807188[WB_DAT_O]=>DRIVER@560386[WB_DAT_O]=>DRIVER@559183[SVCPU_DATA_O_WB]=00001011<=01110011\"",
			"[3] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3886}\"@ 17870000000: DRIVER@591413[SFDATA_MEM_WE]=0<=0\"",
			"[4] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3887}\"@ 17870000000: DRIVER@576815[SVRES]=000001011<=011111110\"",
			"[5] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3888}\"@ 17870000000: DRIVER@591424[SFMUX_CPU_RDY_OUT]=1<=1\"",
			"[6] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3889}\"@ 17870000000: DRIVER@576844[SVOP_MUX_NEW]=11111110<=11111110\"",
			"[7] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3890}\"@ 17870000000: DRIVER@576610[SVINSTRUCTION_NEW]=000100000100111001<=000100000100111001\"",
			"[8] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3891}\"@ 17870000000: DRIVER@807357[SVOUT_DATA]=00001011<=01110011\"",
			"[9] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3892}\"@ 17870000000: DRIVER@591360[SFR_ADDRESS_OUT]=>DRIVER@576475[SVDATA_INTF_SFR_ADDRESS_OUT]=0000100000010101<=0000100011111110\"",
			"[10] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3893}\"@ 17870000000: DRIVER@591415[SVMUX_CPU_DATA_OUT]=00000000<=00000000\"",
			"[11] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3894}\"@ 17870000000: DRIVER@576826[SVFLAG_NEW]=0<=0\"",
			"[12] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3895}\"@ 17870000000: DRIVER@591414[SFDATA_MEM_CE]=0<=0\"",
			"[13] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3896}\"@ 17870000000: DRIVER@591321[DATA_MEM_ADDRESS_OUT]=>DRIVER@575765[DATA_MEM_ADDR_OUT]=>DRIVER@560550[SVCPU_DATACONT_ADDR]=0000100000010101<=0000100011111110\""};

	static String[] delta_2_bug = {"[0] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3855}\"@ 17870000000: DRIVER@581150[SVCODED_CPU_STATE]=001<=100\"",
			"[1] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3856}\"@ 17870000000: DRIVER@576726[SVINSTRUCTION_NEW]=000100000100111001<=000100000100111001\"",
			"[2] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3857}\"@ 17870000000: DRIVER@581291[SVNEW_ADDR]=000100111001<=000100111001\"",
			"[3] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3858}\"@ 17870000000: DRIVER@576931[SVRES]=000001011<=011111110\"",
			"[4] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3859}\"@ 17870000000: DRIVER@581356[SVPREV_ADDRESS_NEW]=000100001111<=000100001111\"",
			"[5] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3860}\"@ 17870000000: DRIVER@591540[SFMUX_CPU_RDY_OUT]=1<=1\"",
			"[6] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3861}\"@ 17870000000: DRIVER@807473[SVOUT_DATA]=ZZZZ1011<=01110011\"",
			"[7] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3862}\"@ 17870000000: DRIVER@591476[SFR_ADDRESS_OUT]=>DRIVER@576591[SVDATA_INTF_SFR_ADDRESS_OUT]=0000100000010101<=0000100011111110\"",
			"[8] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3863}\"@ 17870000000: DRIVER@591530[SFDATA_MEM_CE]=0<=0\"",
			"[9] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3864}\"@ 17870000000: DRIVER@559711[STDDEBUG_PROCESSOR_INTF_OUT]=000100010000<=000100111001\"",
			"[10] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3865}\"@ 17870000000: DRIVER@581304[SVADDR_ADD_1]=000100010001<=000100111010\"",
			"[11] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3866}\"@ 17870000000: DRIVER@581139[CPU_STATE_OUT]=>DRIVER@576337[SSSTATE]=INST_DECODE<=EXECUTE\"",
			"[12] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3867}\"@ 17870000000: DRIVER@591437[DATA_MEM_ADDRESS_OUT]=>DRIVER@575881[DATA_MEM_ADDR_OUT]=>DRIVER@560666[SVCPU_DATACONT_ADDR]=0000100000010101<=0000100011111110\"",
			"[13] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3868}\"@ 17870000000: DRIVER@581250[PROGRAM_COUNTER_OUT]=>DRIVER@576338[SVPROG_COUNTER]=000100010000<=000100111001\"",
			"[14] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3869}\"@ 17870000000: DRIVER@581330[SVPROG_COUNTER_NEW]=000100111001<=000100111001\"",
			"[15] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3870}\"@ 17870000000: DRIVER@591531[SVMUX_CPU_DATA_OUT]=00000000<=00000000\"",
			"[16] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3871}\"@ 17870000000: DRIVER@591529[SFDATA_MEM_WE]=0<=0\"",
			"[17] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3872}\"@ 17870000000: DRIVER@576942[SVFLAG_NEW]=1<=1\"",
			"[18] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3873}\"@ 17870000000: DRIVER@581278[SVJMP_CLASS0]=000100111001<=000100111001\"",
			"[19] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3874}\"@ 17870000000: DRIVER@807304[WB_DAT_O]=>DRIVER@560502[WB_DAT_O]=>DRIVER@559299[SVCPU_DATA_O_WB]=ZZZZ1011<=01110011\"",
			"[20] = {org.zamia.instgraph.sim.ref.IGSignalChangeRequest@3875}\"@ 17870000000: DRIVER@576960[SVOP_MUX_NEW]=11111110<=11111110\""};


}
