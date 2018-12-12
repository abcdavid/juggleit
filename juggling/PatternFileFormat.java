package juggling;

import java.io.*;

public class PatternFileFormat /*implements FileFormat*/ {
	public Pattern readPattern(Reader inputReader) throws IOException {
		return PatternParser.readPattern(inputReader);
	}
	public void writePattern(Writer outputWriter,Pattern pattern) throws IOException {
		BufferedWriter writer=new BufferedWriter(outputWriter);
		String nl=System.getProperty("line.separator");
		String comment=pattern.getComment();
		if (comment!=null) {
			writer.write("/*");
			writer.write(comment);
			writer.write("*/");
			writer.write(nl);
		}
		int jCount=pattern.getJugglerCount();
		writer.write("jugglers {"+jCount+"}"+nl);
		writer.write("hands ");
		for (int h=0;h<pattern.getHandCount();h++) {
			writer.write("{"+pattern.getHand(h).getRhythm().toString()+"}"+nl);
		}
		writer.write("passes ");
		for (int h=0;h<pattern.getHandCount();h++) {
			writer.write("{ ");
			Hand hand=pattern.getHand(h);
			int time=0;
			while (time<pattern.getTotalTime()) {
				if (hand.isBeat(time)) {
					EndPoint passPoint=hand.getEndPoint(time);
					EndPoint catchPoint=passPoint.getPassDestination();
					String passStr;
					if (!catchPoint.isValid()) {
						passStr="0";
					} else {
						int beats=catchPoint.getTime()-passPoint.getTime();
						passStr=Integer.toString(beats)+","+Integer.toString(catchPoint.getHand().getNumber());
					}
					writer.write(passStr+" ");
				}
				time++;
			}
			writer.write("}"+nl);
		}
		if (jCount>1) {
		writer.write("positions {"+nl);
			for (int j=0;j<jCount;j++) {
				Location location=pattern.getJuggler(j).getLocation();
				writer.write("("+location.getX()+","+location.getY()+") "+location.getOrientation());
				writer.write(nl);
			}
			writer.write("}"+nl);
		}
		writer.flush();
		writer.close();
	}
	/*
	public Pattern readInputStream(InputStream input) throws IOException {
		return PatternParser.readPattern(input);
	}
	public void writeOutputStream(OutputStream output,Pattern pattern) throws IOException {
		BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(output));
		String nl=System.getProperty("line.separator");
		int jCount=pattern.getJugglerCount();
		writer.write("jugglers {"+jCount+"}// number of jugglers"+nl);
		writer.write("hands ");
		for (int j=0;j<jCount;j++) {
			Juggler juggler=pattern.getJuggler(j);
			writer.write("{"+juggler.getRightHand().getRhythm().toString()+"}// right hand rhythm"+nl);
			writer.write("{"+juggler.getLeftHand().getRhythm().toString()+"}// left hand rhythm"+nl);
		}
		writer.write("passes ");
		for (int h=0;h<pattern.getHandCount();h++) {
			writer.write("{ ");
			Hand hand=pattern.getHand(h);
			int time=0;
			while (time<pattern.getTotalTime()) {
				if (hand.isBeat(time)) {
					EndPoint passPoint=hand.getEndPoint(time);
					EndPoint catchPoint=passPoint.getPassDestination();
					String passStr;
					if (!catchPoint.isValid()) {
						passStr="0";
					} else {
						int beats=catchPoint.getTime()-passPoint.getTime();
						passStr=Integer.toString(beats)+","+Integer.toString(catchPoint.getHand().getNumber());
					}
					writer.write(passStr+" ");
				}
				time++;
			}
			writer.write("}"+nl);
		}
		writer.flush();
		writer.close();
	}
	*/
}
