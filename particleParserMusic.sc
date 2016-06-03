

(
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
)


(
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
   x = CSVFileReader.read("/home/alex/dev/soundOfCollisions/v2/data.csv").postcs;
)

(

   //var synth = Synth(\grainFM, [\amp, 0]);

   fork {

   // Loop on events
   CSVFileReader.read("/home/alex/dev/soundOfCollisions/v2/data.csv").postcs.do
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

	// End of loop on events
   };

   //synth.release;
	};

""
)



