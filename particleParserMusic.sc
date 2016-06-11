

(
// Les synthétiseurs

SynthDef(\grainFM, {

	arg density  = 30,
	    duration = 0.007,
	    indexFM  = 10,
	    carFM    = 440,
	    modFM    = 220,
	    pan      = 0,
	    amp      = 1,
	    release  = 3,
	    gate     = 1;


	var signal, env;

	signal = GrainFM.ar(2, Dust2.kr(density), duration, carFM, modFM, indexFM, pan, mul: amp);

	env = EnvGen.kr(Env.asr(releaseTime: release), gate, doneAction: 2);

    Out.ar(0,signal * env)

}).add;

SynthDef(\grainFMperc, {

	arg density  = 30,
	    duration = 0.007,
	    indexFM  = 10,
	    carFM    = 440,
	    modFM    = 220,
	    pan      = 0,
	    amp      = 1,
	    dur      = 3,
	    gate     = 1;


	var signal, env;

	signal = GrainFM.ar(2, Dust2.kr(density), duration, carFM, modFM, indexFM, pan, mul: amp);

	env = EnvGen.kr(Env.perc, gate, timeScale:dur, doneAction: 2);

    Out.ar(0,signal * env)

}).add;
)


Synth(\grainFMperc)




x = Synth(\grainFM)

x.release()
x.set(\amp, 0.8)



(
x = {GrainFM.ar(2,
	            trigger:Dust2.kr(MouseX.kr(3,300)),
	            dur:0.007,
	            index: MouseY.kr(1,30),
	            carfreq: 440,
	            modfreq: 220
     )}.play
)



(
   x = CSVFileReader.read( (PathName(thisProcess.nowExecutingPath).pathOnly ++ "data/data.csv").standardizePath).postcs;
)



(
   //var synth = Synth(\grainFM, [\amp, 0]);

   fork {

   // Loop on event
   CSVFileReader.read((PathName(thisProcess.nowExecutingPath).pathOnly ++ "data/data.csv").standardizePath).postcs.do
   {

	  arg event;

	  var nParticles = asInteger(event[0]);

	  event.postln;

	  // Loop on particles in the event
	  if (nParticles > 0, {
	  for (0, asInteger(event[0])-1,
	  {
	    arg p;

		var type   = event[1 + (p * 4) + 0];
		var energy = event[1 + (p * 4) + 1].asFloat;
    	var eta    = event[1 + (p * 4) + 2].asFloat;
		var phi    = event[1 + (p * 4) + 3].asFloat;
		var density = 30;




				p.postln;
				energy.postln;

				density = switch(type)
					      {"J"}{150}
					      {"M"}{10}
					      {"E"}{50}
					      {"P"}{25};

				//synth.set(\amp, energy.linlin(0,300,0.1,1));

				Synth(\grainFMperc, [\amp,     energy.linlin(0,300,0.15,0.7),
						             \dur,     energy.linlin(0,300,1,5),
						             \carFM,   eta.linexp(0,5,10000,50),
						             \indexFM, energy.linlin(0,300,3,50),
						             \pan,     cos(phi),
					                 \density, density]);

				/*
				density  = 30,
				duration = 0.007,
				indexFM  = 10,
				carFM    = 440,
				modFM    = 220,
				pan      = 0,
				amp      = 1,
				release  = 3,
				gate     = 1;
				*/



	// end of fork



    // End of loop on particles inside current event
	})});

	(nParticles/2).wait;



	//synth.set(\amp, 0);

	// End of loop on event
   };

   //synth.release;
	};

""
)

//////////////////////////////////////////////////////////////////////
// other timbre
/*
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
You need install SuperCollider Plugins : https://github.com/supercollider/sc3-plugins
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
*/

(
SynthDef(\gendy, { |out=0,freq=200, freqResonz=4000, bwr=5, modulation=1, pos=0, dur=3, amp=1|
	var n, sig, env;
	n=10;

	env = EnvGen.kr(Env.perc, levelScale:amp, timeScale:dur, doneAction:2);

	sig = Pan2.ar(
		Resonz.ar(
			Mix.fill(n,{
				var numcps = rrand(2,20);

				Gendy1.ar(
					6.rand,6.rand,
					1.0.rand,1.0.rand,
					freq, freq*1.5,
					1.0.rand, 1.0.rand, numcps,
					SinOsc.kr(modulation, 0, numcps/2, numcps/2),
					1/(n.sqrt)
				)
			}),
			freqResonz, bwr),
		pos, 5);

	sig = Limiter.ar(sig, 0.6);

	Out.ar(out, sig * env)
}).add;
)


Synth(\gendy, [\amp,30, \modulation,100]);



(
var type, energy, eta, phi;
var energyMin, energyMax;
var data;


"import data".postln;
data = CSVFileReader.read( (PathName(thisProcess.nowExecutingPath).pathOnly ++ "data/data.csv").standardizePath);



data.do({ arg event;
	var nParticles = asInteger(event[0]);

	if (nParticles > 0, {
		forBy(2, 4, event.size-1, { |i|
			energy = energy.add(event[i].asFloat);
		});
	});
});

energyMin = energy.min; energyMax = energy.max;
// postf("energyMin = % -- energyMax = %\n", energyMin, energy.max);



fork {
	data.do({ arg event;
		var nParticles = asInteger(event[0]);

		event.postln;

		// Loop on particles in the event
		if (nParticles > 0, {
			forBy(1, 4, event.size-1, { |i|
				type = switch(event[i])
					      {"M"}{1}
					      {"P"}{2}
					      {"E"}{3}
					      {"J"}{4};
				energy = event[i+1].asFloat;
				eta = event[i+2].asFloat;
				phi = event[i+3].asFloat;

				Synth(\gendy, [
					// \freq, 200,
					\freq, type * eta.abs.linlin(0, pi, 40, 200),
					\freqResonz, 4000,
					\bwr, eta.abs.linlin(0, pi, 5, 0.5),
					\modulation, switch(type)
					      {1}{0.05}
					      {2}{5}
					      {3}{50}
					      {4}{150},
					\pos, cos(phi),
					\dur, energy.linlin(energyMin, energyMax, 0.5, 5),
					\amp, energy.linlin(energyMin, energyMax, 0.05,1)
				]);
			});
		}, {energy = (energyMin+energyMax)/2});

		energy.linlin(energyMin, energyMax, 0.15, 0.6).wait;
	});
};
)


/*
[ 1, 			M, 		120.350,	-1.772, 1.863 ]
[nbParticules,	type,	energy,			eta,	phi]


density = switch(type)
{"J"}{150}
{"M"}{10}
{"E"}{50}
{"P"}{25};
*/


(
var type, energy, eta, phi;
var energyMin, energyMax;
var data;


"import data".postln;
data = CSVFileReader.read( (PathName(thisProcess.nowExecutingPath).pathOnly ++ "data/data.csv").standardizePath);



data.do({ arg event;
	var nParticles = asInteger(event[0]);

	if (nParticles > 0, {
		forBy(2, 4, event.size-1, { |i|
			energy = energy.add(event[i].asFloat);
		});
	});
});

energyMin = energy.min; energyMax = energy.max;
// postf("energyMin = % -- energyMax = %\n", energyMin, energy.max);



fork {
	data.do({ arg event;
		var nParticles = asInteger(event[0]);

		event.postln;

		// Loop on particles in the event
		if (nParticles > 0, {
			forBy(1, 4, event.size-1, { |i|
				type = event[i];
				energy = event[i+1].asFloat;
				eta = event[i+2].asFloat;
				phi = event[i+3].asFloat;

				Synth(\gendy, [
					\freq, 200,
					\freqResonz, 4000,
					\bwr, eta.abs.linlin(0, pi, 5, 0.5),
					\modulation, switch(type)
					      {"M"}{0.1}
					      {"P"}{5}
					      {"E"}{50}
					      {"J"}{150},
					\pos, cos(phi),
					\dur, energy.linlin(energyMin, energyMax, 0.5, 5),
					\amp, energy.linlin(energyMin, energyMax, 0.1,1)
				]);
			});
		}, {energy = [energyMin, energyMax].mean});

		energy.linlin(energyMin, energyMax, 0.1, 0.6).wait;
	});
};
)



