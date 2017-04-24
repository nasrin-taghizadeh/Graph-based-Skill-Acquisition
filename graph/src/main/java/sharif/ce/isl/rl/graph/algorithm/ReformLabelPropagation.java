package sharif.ce.isl.rl.graph.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sharif.ce.isl.rl.graph.algorithm.core.MarkovOption;
import sharif.ce.isl.rl.graph.algorithm.core.SubgoalBasedQLearner;
import sharif.ce.isl.rl.graph.algorithm.core.Trajectory;
import sharif.ce.isl.rl.graph.environment.Environment;
import sharif.ce.isl.rl.graph.environment.State;

class Community {
	public int CommunityID;
	public int CommunityLabel;
	public List<Integer> Nodes = new ArrayList<Integer>();
	public List<Community_Link> links = new ArrayList<Community_Link>();
}

class Community_Link {
	public List<Integer> ArrayLinkNode = new ArrayList<Integer>();
	public int connected_CommunityID;
}

public class ReformLabelPropagation extends SubgoalBasedQLearner {

	protected int[] NodeLabel;
	protected int[][] neighbour;
	protected int[] LabelNum;
	int MaxNeighbor = 50;
	public List<Community> communityList;
	////////////////////////////////////////////////////
	public int NumCommunity = 180;
	public int NumCommunities;
	public double[][] CommunityLink;
	int[] NodeExist;
	protected List<option> optionList;
	List<MarkovOption> AddOptions = new ArrayList<MarkovOption>();

	public ReformLabelPropagation(Environment earth) {
		super(earth, 2);

		NodeLabel = new int[maxStateID];
		neighbour = new int[maxStateID][MaxNeighbor];
		LabelNum = new int[MaxNeighbor];

		for (int i = 0; i < maxStateID; i++)
			for (int j = 0; j < MaxNeighbor; j++)
				neighbour[i][j] = -1;

		for (int i = 0; i < maxStateID; i++)
			NodeLabel[i] = i;

		for (int i = 0; i < MaxNeighbor; i++)
			LabelNum[i] = 0;
	}

	@Override
	public String getName() {
		return "RefLabel";
	}

	public void FindCommunity() {
		communityList = new ArrayList<Community>();
		int CommunityID = 0;
		for (int i = 0; i < maxStateID; i++) {
			if (NodeExist[i] == 1) {
				int flag = 0;
				for (Community com : communityList)
					if (NodeLabel[i] == com.CommunityLabel) {
						com.Nodes.add(i);
						flag = 1;
					}
				if (flag == 0) {
					Community c = new Community();
					c.CommunityID = CommunityID;
					c.CommunityLabel = NodeLabel[i];
					c.Nodes.add(i);
					communityList.add(c);
					CommunityID++;
				}
			}
		}
		NumCommunities = CommunityID;
		System.out.println("NumCommunities: " + NumCommunities);
	}

	public void CreateCommunitiesLinks() {
		for (Community com1 : communityList)
			for (Community com2 : communityList)
				for (int i : com1.Nodes)
					for (int j : com2.Nodes)
						if (edgeWeights[i][j] != 0 && i != j) {
							int flag = 0;

							if (com1 != com2) {
								for (Community_Link ComLink : com1.links)
									if (ComLink.connected_CommunityID == com2.CommunityID) {
										flag = 1;
										ComLink.ArrayLinkNode.add(i); /// !!!!!!!!!!!!!!
									}
								if (flag == 0) {
									Community_Link ComLink = new Community_Link();
									ComLink.ArrayLinkNode.add(i);
									ComLink.connected_CommunityID = com2.CommunityID;
									com1.links.add(ComLink);
								}
							}
							communityList.set(communityList.indexOf(com1), com1);
						}
	}

	public void CalculateQ() {
		CommunityLink = new double[NumCommunities][NumCommunities];
		// CommunityLinkEdges=new ArrayList<Integer>
		// [NumCommunities][NumCommunities];
		for (int i = 0; i < NumCommunities; i++)
			for (int j = 0; j < NumCommunities; j++)
				CommunityLink[i][j] = 0;

		for (Community com1 : communityList)
			for (Community com2 : communityList)
				for (int i : com1.Nodes)
					for (int j : com2.Nodes)
						if (edgeWeights[i][j] != 0 && i != j)
							CommunityLink[com1.CommunityID][com2.CommunityID]++;
		/*
		 * for (Community com1:communityList) for (Community com2:communityList)
		 * CommunityLink[com1.CommunityID][com2.CommunityID]/=2.0;
		 */
		int E = 0;
		for (int i = 0; i < maxStateID; i++)
			for (int j = 0; j < i; j++)
				if (edgeWeights[i][j] != 0)
					E++;
		double Q = 0, Q1, Q2;
		for (Community com1 : communityList) {
			Q2 = 0;
			CommunityLink[com1.CommunityID][com1.CommunityID] /= 2.0;
			Q1 = CommunityLink[com1.CommunityID][com1.CommunityID];
			for (Community com2 : communityList)
				if (com1 != com2)
					Q2 += CommunityLink[com1.CommunityID][com2.CommunityID];
			// CommunityLink[com2.CommunityID][com1.CommunityID];
			// Q+=Q1/E-((2*Q1+Q2)/(2*E))*((2*Q1+Q2)/(2*E));
			Q += Q1 / E - ((Q1 + Q2) / E) * ((Q1 + Q2) / E);
			// System.out.println("hello"+Q);
		}
		// System.out.println("Q="+Q);
	}

	public void MergeCommunity(int ComLinkIndex1, int ComLinkIndex2) {
		Community NewCommunity1 = new Community();
		NewCommunity1 = communityList.get(ComLinkIndex1);
		// System.out.println("first community for merge is
		// "+NewCommunity1.CommunityLabel);
		Community NewCommunity2 = new Community();
		NewCommunity2 = communityList.get(ComLinkIndex2);
		// System.out.println("second community for merge is
		// "+NewCommunity2.CommunityLabel);
		Community firstCommunity = new Community();
		Community secondCommunity = new Community();
		if (NewCommunity1.CommunityID < NewCommunity2.CommunityID) {
			firstCommunity = NewCommunity1;
			secondCommunity = NewCommunity2;
		} else {
			firstCommunity = NewCommunity2;
			secondCommunity = NewCommunity1;
		}
		for (Integer i : secondCommunity.Nodes)
			firstCommunity.Nodes.add(i);
		//////// az firstCommunity link secondCommunity hazf
		//////// shavad//////////////
		// System.out.println(firstCommunity.links.size());
		int LinkIndex = 0;
		for (Community_Link ComLink : firstCommunity.links) {
			if (ComLink.connected_CommunityID == secondCommunity.CommunityID) {
				// System.out.println("YYYYYYYYYYYYY");
				break;
			}
			LinkIndex++;
		}
		// System.out.println("LinkIndex="+LinkIndex);
		firstCommunity.links.remove(LinkIndex);
		////////////////////// be firstCommunity linkhay secondCommunity ezafe
		////////////////////// shavad/////////////////////////////////////////////////
		for (Community_Link ComLink : secondCommunity.links) {
			int flag = 0;
			for (Community_Link ComLink1 : firstCommunity.links)
				if (ComLink.connected_CommunityID == ComLink1.connected_CommunityID) {
					flag = 1;
					for (int i : ComLink.ArrayLinkNode)
						ComLink1.ArrayLinkNode.add(i);
					break;
				}
			if (flag == 0)
				if (ComLink.connected_CommunityID != firstCommunity.CommunityID)
					firstCommunity.links.add(ComLink);
		}

		communityList.set(communityList.indexOf(firstCommunity), firstCommunity);
		communityList.remove(secondCommunity);
		///////////////////////////////////////////////////////////////////////
		// communityList.add(firstCommunity);
		/*
		 * if (ComLinkIndex1<ComLinkIndex2) {
		 * communityList.remove(ComLinkIndex1);
		 * communityList.remove(ComLinkIndex2-1); } else {
		 * communityList.remove(ComLinkIndex2);
		 * communityList.remove(ComLinkIndex1-1); }
		 */
		/////// har ja link be secondCommunity vojod darad in link be
		/////// firstCommunity mortabet shavad///
		/////////////// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%/////////////////
		for (Community c : communityList) {
			int ComIndex = 0;
			int flag = 0;
			for (Community_Link comLink : c.links) {
				if (comLink.connected_CommunityID == secondCommunity.CommunityID) {
					flag = 1;
					break;
					// comLink.connected_CommunityID=firstCommunity.CommunityID;
				}
				ComIndex++;
			}
			if (flag == 1) {
				int ff = 0;
				int firstIndex = 0;
				for (Community_Link com_Link : c.links) {
					if (com_Link.connected_CommunityID == firstCommunity.CommunityID) {
						ff = 1;
						break;
					}
					firstIndex++;
				}
				if (ff == 0) {
					Community_Link NewComLink = new Community_Link();
					NewComLink = c.links.get(ComIndex);
					NewComLink.connected_CommunityID = firstCommunity.CommunityID;
					c.links.set(ComIndex, NewComLink);
				} else {
					Community_Link NewComLink = new Community_Link();
					NewComLink = c.links.get(ComIndex);
					Community_Link NewComLink2 = new Community_Link();
					NewComLink2 = c.links.get(firstIndex);
					for (int ComL : NewComLink.ArrayLinkNode)
						NewComLink2.ArrayLinkNode.add(ComL);
					c.links.set(firstIndex, NewComLink2);
					c.links.remove(ComIndex);
				}
			}
		}
		/////////////////// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%/////////////////
		int ComLinkIndex, Comindex;
		for (Community c : communityList) {
			// System.out.println(c.CommunityID+":"+c.CommunityLabel+":");
			if (c.CommunityID > secondCommunity.CommunityID) {
				for (Community Com : communityList)
					for (Community_Link ComLink : Com.links)
						if (ComLink.connected_CommunityID == c.CommunityID) {
							// ComLink.connected_CommunityID--;
							Comindex = communityList.indexOf(Com);
							ComLinkIndex = Com.links.indexOf(ComLink);
							Community_Link ACommunity = Com.links.get(ComLinkIndex);
							ACommunity.connected_CommunityID--;
							Com.links.set(ComLinkIndex, ACommunity);
							communityList.set(Comindex, Com);
						}
				ComLinkIndex = communityList.indexOf(c);
				// communityList.get(ComLinkIndex).CommunityID--;
				Community ACommunity = communityList.get(ComLinkIndex);
				ACommunity.CommunityID--;
				communityList.set(ComLinkIndex, ACommunity);
			}
		}

		/////////////////// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%/////////////////
		NumCommunities--;
		/*
		 * for (Community c:communityList) {
		 * System.out.print(c.CommunityID+":"+c.CommunityLabel+":"); for (int
		 * i:c.Nodes) System.out.print(i+" "); System.out.println(); for
		 * (Community_Link ComLink:c.links)
		 * System.out.print(ComLink.connected_CommunityID+"  ");
		 * System.out.println(); }
		 */
		// System.out.println("zzz");
	}

	///////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////

	double DeltaQ(Community com1, Community com2) {
		double Ai = 0, Aj = 0, deltaQ;
		int E = 0;
		for (int i = 0; i < maxStateID; i++)
			for (int j = 0; j < i; j++)
				if (edgeWeights[i][j] != 0)
					E++;
		// E*=2;
		// System.out.println("E: "+E);
		for (Community c : communityList) {
			if (com1 != c)
				Ai += CommunityLink[com1.CommunityID][c.CommunityID] / 2.0;
			else
				Ai += CommunityLink[com1.CommunityID][c.CommunityID];
			if (com2 != c)
				Aj += CommunityLink[com2.CommunityID][c.CommunityID] / 2.0;
			else
				Aj += CommunityLink[com2.CommunityID][c.CommunityID];
		}
		// deltaQ=CommunityLink[com1.CommunityID][com2.CommunityID]/E-2*Ai/E*Aj/E;
		// System.out.println("comLink:
		// "+CommunityLink[com1.CommunityID][com2.CommunityID]);
		// System.out.println("2e:
		// "+2*CommunityLink[com1.CommunityID][com2.CommunityID]/E);
		// System.out.println("aa: "+2*(Ai/E)*(Aj/E));
		deltaQ = (2 * CommunityLink[com1.CommunityID][com2.CommunityID] / E) - (2 * (Ai / E) * (Aj / E));
		// System.out.println(com1.CommunityLabel+" "+com2.CommunityLabel+"
		// deltaQ="+deltaQ);
		return deltaQ;

	}

	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	public void MergeCommunity() {
		int counter = 0;
		int f = 1;
		while (f == 1) {
			double MaxDelta = 0;
			int MaxDeltaIndex1, MaxDeltaIndex2;
			Community MaxDeltaCommunity1, MaxDeltaCommunity2;
			MaxDeltaCommunity1 = new Community();
			MaxDeltaCommunity2 = new Community();
			f = 0;
			for (Community com1 : communityList)
				for (Community com2 : communityList) {
					if (com1.CommunityID == com2.CommunityID)
						continue;
					int flag = 0;
					for (Community_Link ComLink1 : com1.links)
						for (Community_Link ComLink2 : com2.links)
							if (ComLink1.connected_CommunityID == ComLink2.connected_CommunityID) {
								flag = 1;
								// System.out.println("flag =1");
							}

					// if (com1!=com2 && DeltaQ(com1,com2)>MaxDelta)
					if (flag == 1 && DeltaQ(com1, com2) > MaxDelta && communityList.size() > 2) {
						// System.out.println("max delta to be chabged");
						f = 1;
						MaxDelta = DeltaQ(com1, com2);
						MaxDeltaCommunity1 = com1;
						MaxDeltaCommunity2 = com2;
						// MaxDeltaIndex1=communityList.indexOf(com1);
						// MaxDeltaIndex2=communityList.indexOf(com2);
					}
					// else{
					// System.out.println("no merge:");
					// System.out.println("flag: "+flag);
					// System.out.println("id: "+com1.CommunityID+"
					// "+com2.CommunityID);
					// System.out.println(DeltaQ(com1,com2)+" < "+MaxDelta);
					// System.out.println("comm size: "+communityList.size());
					// }
				}
			/////////////////////////////////////////////////////////////////

			if (f == 1) {
				// System.out.println(MaxDeltaCommunity1.CommunityLabel+"
				// "+MaxDeltaCommunity2.CommunityLabel);
				MaxDeltaIndex1 = communityList.indexOf(MaxDeltaCommunity1);
				MaxDeltaIndex2 = communityList.indexOf(MaxDeltaCommunity2);
				MergeCommunity(MaxDeltaIndex1, MaxDeltaIndex2);
				counter++;
				CalculateQ();
			}
			// else
			// System.out.println("stop");

		}
		// DeltaQ(NewCommunity1,NewCommunity2);

		System.out.println("NumCommunities after merge: " + NumCommunities);

	}

	/////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////
	public void CommunityDetection() {
		int k;
		int[] maxLabelNumIndex = new int[maxStateID];
		List<Integer> MaxLabel = new ArrayList<Integer>();
		NodeExist = new int[maxStateID];
		int[] NumNeighbour = new int[maxStateID];
		int[] PreNodeLabel = new int[maxStateID];
		for (int i = 0; i < maxStateID; i++)
			NodeExist[i] = 0;

		for (int i = 0; i < maxStateID; i++) {
			k = 0;
			// neighbour[i][k++]=i; //khodash ra dar nazar nagirim
			for (int j = 0; j < maxStateID; j++) {
				if (edgeWeights[i][j] != 0 && i != j) {
					neighbour[i][k++] = j;// tedad hamsaieha kamtar az 5 ast =k
					NodeExist[i] = 1; // agar hamsaie darad pas vojod darad
				}
			}
			if (NodeExist[i] == 1)
				NumNeighbour[i] = k;
		}
		///////////////////// numOfColumns////////////////////////
		int f;
		for (int x = 0; x < 4000; x++) {
			// System.out.println();
			f = 0;
			for (int i = 0; i < maxStateID; i++)
				PreNodeLabel[i] = NodeLabel[i];
			for (int i = 0; i < maxStateID; i++) {
				if (NodeExist[i] == 1) {
					for (int j = 0; j < NumNeighbour[i]; j++) {
						for (int l = j; l < NumNeighbour[i]; l++) // az j shoroa
																	// shode pas
																	// har
																	// barchasb
																	// hadeaghl
																	// iki hast
						{
							if (NodeLabel[neighbour[i][j]] == NodeLabel[neighbour[i][l]])
								LabelNum[j]++;
						}
					}

					int maxLabelNum = LabelNum[0];
					// maxLabelNumIndex[i]=neighbour[i][0];
					for (int j = 1; j < NumNeighbour[i]; j++) {
						if (LabelNum[j] > maxLabelNum) {
							maxLabelNum = LabelNum[j];
							// maxLabelNumIndex[i]=neighbour[i][j];
							/////////
						}
					}

					for (int j = 0; j < NumNeighbour[i]; j++) {
						if (LabelNum[j] == maxLabelNum)
							MaxLabel.add(NodeLabel[neighbour[i][j]]); //
					}
					maxLabelNumIndex[i] = MaxLabel.get(rand.nextInt(MaxLabel.size()));

					NodeLabel[i] = maxLabelNumIndex[i];

					MaxLabel = new ArrayList<Integer>();

					for (int h = 0; h < MaxNeighbor; h++)
						LabelNum[h] = 0;

				} // end if

			} // end for i
			/////////////////////// numOfColumns//////////////////////

			for (int i = 0; i < maxStateID; i++) {
				/*
				 * if (NodeExist[i]==1){ if (i%(numOfColumns)==1)
				 * System.out.println(); System.out.print(NodeLabel[i]+"\t"); }
				 * else System.out.print("\t");
				 */
				if (PreNodeLabel[i] != NodeLabel[i])
					f = 1;
			}
			// System.out.println();

			if (f == 0) {
				// System.out.println("end stage is "+x);
				break;
			}
			/////////////////////// numOfColumns//////////////////////
			////////////// har node max label hamsaiesh chist/////////////
		} // end x

		// for (int i=0;i<maxStateID;i++)
		// if (NodeExist[i]==1)
		// System.out.println(i+":"+NodeLabel[i]+ " ");
	}
	////////////////////// label propagation////

	public void RepeatCommunityDetection() {
		CommunityDetection();

	}
	/////////////////////////////////////////////

	public void CreateOptionCoummunity() {
		optionList = new ArrayList<option>();
		int counter = 0;

		for (Community com : communityList) {
			// List<Integer> CommunityNeighbor=new ArrayList<Integer>();
			for (Community_Link comLink : com.links) {
				if (IsTrueFinalState(comLink.ArrayLinkNode, com.Nodes)) {
					// CommunityNeighbor.add(comLink.connected_CommunityID);
					option op = new option();
					int linkNode = comLink.ArrayLinkNode.get(rand.nextInt(comLink.ArrayLinkNode.size()));
					op.finalState.add(stateTable.get(linkNode));
					op.optionID = counter;
					// op.optionEdge=l.edgeLink;
					for (int s : com.Nodes)
						op.initialState.add(stateTable.get(s));
					op.selected = 0;
					optionList.add(op);
					counter++;
				}
			}
		}
		/*
		 * for(Community com:communityList) { int max=0; Community_Link
		 * maxComLink=null; for (Community_Link comLink:com.links) { int
		 * num=NumInitiationSet(comLink.ArrayLinkNode,com.Nodes); if (num>max) {
		 * max=num; maxComLink=comLink; } } option op=new option(); int
		 * linkNode=maxComLink.ArrayLinkNode.get(rand.nextInt(maxComLink.
		 * ArrayLinkNode.size()));
		 * op.finalState.add(logger.allObservedStates.get(linkNode));
		 * op.optionID=counter; // op.optionEdge=l.edgeLink; for(int
		 * s:com.Nodes) op.initialState.add(logger.allObservedStates.get(s));
		 * op.selected=0; optionList.add(op); counter++; }
		 */
		// System.out.println("num options="+counter);
	}
	///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////

	boolean IsTrueFinalState(List<Integer> ArraySubGoal, List<Integer> ClusterSet) {
		int c = 0;
		HashMap<Integer, State> ObservedStates = new HashMap<Integer, State>();
		for (int SubGoal : ArraySubGoal) {
			List<State> PreviousStates = InitiationSet(stateTable.get(SubGoal), Math.sqrt(ClusterSet.size()) / 2);
			for (State PreviousState : PreviousStates)
				ObservedStates.put(PreviousState.ID(), PreviousState);
		}
		/*
		 * System.out.print("ObservedStates= "); for (State
		 * state1:ObservedStates.values()) System.out.print(state1.ID()+" ");
		 * System.out.println();
		 * 
		 * System.out.print("ClusterSet= "); for (Integer state1:ClusterSet)
		 * System.out.print(state1+" "); System.out.println();
		 */
		for (State state1 : ObservedStates.values())
			for (Integer state2 : ClusterSet)
				// if (ClusterSet.contains(state))
				if (state1.ID() == state2)
					c++;
		int min;
		if (ObservedStates.size() < ClusterSet.size())
			min = ObservedStates.size();
		else
			min = ClusterSet.size();

		if (c > min / 2)
			return true;
		else
			return false;
	}

	List<State> InitiationSet(State subgoal, double Trajectorylength) {
		List<StateFrequency> PreviousState;
		PreviousState = new ArrayList<StateFrequency>();
		for (Trajectory T : ListTrajectory) {
			int flag = 0, counter = 0, c1 = 0;
			for (State s : T.AllStates) {
				counter++;
				if (subgoal == null)
					System.out.println("subgoal is null");
				if (s.ID() == subgoal.ID())
					flag = 1;
				if (flag == 1)
					break;
			}
			if (flag == 1) {
				for (State s : T.AllStates) {
					c1++;
					if (c1 < counter && c1 > counter - Trajectorylength) /// inja
																			/// fekr
																			/// konam
					{
						// System.out.print("("+s.x+","+s.y+") ");
						int f2 = 0;

						for (StateFrequency preState : PreviousState)
							if (s.ID() == preState.state.ID()) {
								int IndexPreviousState = PreviousState.indexOf(preState);
								f2 = 1;
								int fr = preState.frequency;
								StateFrequency StateFr = new StateFrequency(preState.state, fr + 1);
								PreviousState.set(IndexPreviousState, StateFr);
								;
							}
						if (f2 == 0) {
							StateFrequency preState = new StateFrequency(s, 1);
							PreviousState.add(preState);
						}
					}
				}
			}
		}
		for (int i = 0; i < PreviousState.size(); i++)
			for (int j = 0; j < PreviousState.size() - i - 1; j++) {
				if (PreviousState.get(j).frequency < PreviousState.get(j + 1).frequency) {
					StateFrequency StateFr = PreviousState.get(j);
					PreviousState.set(j, PreviousState.get(j + 1));
					PreviousState.set(j + 1, StateFr);
				}
			}
		List<State> InitialState = new ArrayList<State>();
		for (int i = 0; i < PreviousState.size(); i++)
			InitialState.add(PreviousState.get(i).state);
		return InitialState;
	}

	int NumInitiationSet(List<Integer> ArraySubGoal, List<Integer> ClusterSet) {
		int c = 0;
		HashMap<Integer, State> ObservedStates = new HashMap<Integer, State>();
		for (int SubGoal : ArraySubGoal) {
			List<State> PreviousStates = InitiationSet(stateTable.get(SubGoal), Math.sqrt(ClusterSet.size()));
			for (State PreviousState : PreviousStates)
				ObservedStates.put(PreviousState.ID(), PreviousState);
		}
		// System.out.print("ObservedStates= ");
		for (State state1 : ObservedStates.values())
			System.out.print(state1.ID() + " ");

		System.out.print("ClusterSet= ");

		for (State state1 : ObservedStates.values())
			for (Integer state2 : ClusterSet)
				if (state1.ID() == state2)
					c++;
		return c;
	}

	public List<MarkovOption> CreateOptions() {
		List<MarkovOption> options = new ArrayList<MarkovOption>();
		int ID = 0;
		for (option op1 : optionList) {
			MarkovOption newOption = new MarkovOption(op1.initialState, op1.finalState, environment.ActionSet().size(),
					maxStateID, /* alpha */ .1, gamma, environment);
			options.add(newOption);
			for (Object state : op1.initialState) {
				// OptionLength newOptionLength=new
				// OptionLength((State)state,MaxOptionLength);
				// newOption.OptionDistanceToSubGoal.add(newOptionLength);
				AddApplicableOptionForState(newOption, ((State) state).ID());
			}
			ID++;
		}
		AddOptions = options;
		// SumOptionStateValues=new double[AddOptions.size()];
		myGeneratedOptions = AddOptions;
		System.out.println("num of options: " + options.size());
		return AddOptions;
	}

	@Override
	public void learnOptions() {
		CommunityDetection();
		FindCommunity();
		CreateCommunitiesLinks();
		CalculateQ();
		MergeCommunity();
		CreateOptionCoummunity();
		CreateOptions();
		ExperienceReplay();
	}

}
