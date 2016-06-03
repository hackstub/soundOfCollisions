#! /usr/bin/env python2.6

import sys
import ROOT as rt



def stringify(a) :
    return "%.3f" % a
    




inputFile = sys.argv[1]
treeName  = sys.argv[2]

f = rt.TFile(inputFile)

tree = f.Get(treeName)

for event in tree :

    nJet      = event.NJet
    nMuon     = event.NMuon
    nElectron = event.NElectron
    nPhoton   = event.NPhoton
    
    nParticles = nJet + nMuon + nElectron + nPhoton

    L = [ ]

    L.append(str(nParticles))

    for i in range(nJet) :

        jet = rt.TLorentzVector(event.Jet_Px[i],event.Jet_Py[i],event.Jet_Pz[i],event.Jet_E[i]); 
        
        L.append("J")
        L.append(stringify(jet.E()))
        L.append(stringify(jet.Eta()))
        L.append(stringify(jet.Phi()))

    for i in range(nElectron) :

        electron = rt.TLorentzVector(event.Electron_Px[i],event.Electron_Py[i],event.Electron_Pz[i],event.Electron_E[i]); 
        
        L.append("E")
        L.append(stringify(electron.E()))
        L.append(stringify(electron.Eta()))
        L.append(stringify(electron.Phi()))

    for i in range(nMuon) :

        muon = rt.TLorentzVector(event.Muon_Px[i],event.Muon_Py[i],event.Muon_Pz[i],event.Muon_E[i]); 
        
        L.append("M")
        L.append(stringify(muon.E()))
        L.append(stringify(muon.Eta()))
        L.append(stringify(muon.Phi()))

    for i in range(nPhoton) :

        photon = rt.TLorentzVector(event.Photon_Px[i],event.Photon_Py[i],event.Photon_Pz[i],event.Photon_E[i]); 
        
        L.append("P")
        L.append(stringify(photon.E()))
        L.append(stringify(photon.Eta()))
        L.append(stringify(photon.Phi()))


    print ",".join(L)



