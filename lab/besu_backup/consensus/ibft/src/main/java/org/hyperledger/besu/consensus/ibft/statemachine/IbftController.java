/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.consensus.ibft.statemachine;


import org.hyperledger.besu.consensus.ibft.ConsensusRoundIdentifier;
import org.hyperledger.besu.consensus.ibft.Gossiper;
import org.hyperledger.besu.consensus.ibft.MessageTracker;
import org.hyperledger.besu.consensus.ibft.SynchronizerUpdater;
import org.hyperledger.besu.consensus.ibft.blockcreation.IbftBlockCreator;
import org.hyperledger.besu.consensus.ibft.blockcreation.IbftBlockCreatorFactory;
import org.hyperledger.besu.consensus.ibft.ibftevent.BlockTimerExpiry;
import org.hyperledger.besu.consensus.ibft.ibftevent.IbftReceivedMessageEvent;
import org.hyperledger.besu.consensus.ibft.ibftevent.NewChainHead;
import org.hyperledger.besu.consensus.ibft.ibftevent.RoundExpiry;
import org.hyperledger.besu.consensus.ibft.messagedata.CommitMessageData;
import org.hyperledger.besu.consensus.ibft.messagedata.IbftV2;
import org.hyperledger.besu.consensus.ibft.messagedata.PrepareMessageData;
import org.hyperledger.besu.consensus.ibft.messagedata.ProposalMessageData;
import org.hyperledger.besu.consensus.ibft.messagedata.RoundChangeMessageData;
import org.hyperledger.besu.consensus.ibft.messagewrappers.IbftMessage;
import org.hyperledger.besu.consensus.ibft.payload.Authored;
import org.hyperledger.besu.ethereum.chain.Blockchain;
import org.hyperledger.besu.ethereum.chain.MinedBlockObserver;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPool;
import org.hyperledger.besu.ethereum.mainnet.BlockHeaderValidator;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Message;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.MessageData;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.time.Clock;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.TimerTask;
import java.util.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.util.Subscribers;

public class IbftController {

  private static final Logger LOG = LogManager.getLogger();
  private final Blockchain blockchain;
  private final IbftFinalState ibftFinalState;
  private final IbftBlockHeightManagerFactory ibftBlockHeightManagerFactory;
  private final FutureMessageBuffer futureMessageBuffer;
  private BlockHeightManager currentHeightManager;
  private final Gossiper gossiper;
  private final MessageTracker duplicateMessageTracker;
  private final SynchronizerUpdater sychronizerUpdater;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final int agentStatus;
  private final TransactionPool transactionPool;
  private final DataOutputStream out;
  private final DataInputStream in;
  private final IbftBlockCreatorFactory ibftBlockCreatorFactory;
  private final Subscribers<MinedBlockObserver> observers;

  public IbftController(
          final Blockchain blockchain,
          final IbftFinalState ibftFinalState,
          final IbftBlockHeightManagerFactory ibftBlockHeightManagerFactory,
          final Gossiper gossiper,
          final MessageTracker duplicateMessageTracker,
          final FutureMessageBuffer futureMessageBuffer,
          final SynchronizerUpdater sychronizerUpdater,
          final int agentStatus,
          final TransactionPool transactionPool,
          final DataOutputStream out,
          final DataInputStream in,
          final IbftBlockCreatorFactory ibftBlockCreatorFactory,
          final Subscribers<MinedBlockObserver> observers
          ) {
    this.blockchain = blockchain;
    this.ibftFinalState = ibftFinalState;
    this.ibftBlockHeightManagerFactory = ibftBlockHeightManagerFactory;
    this.futureMessageBuffer = futureMessageBuffer;
    this.gossiper = gossiper;
    this.duplicateMessageTracker = duplicateMessageTracker;
    this.sychronizerUpdater = sychronizerUpdater;
    this.agentStatus = agentStatus;
    this.transactionPool = transactionPool;
    this.out = out;
    this.in = in;
    this.ibftBlockCreatorFactory = ibftBlockCreatorFactory;
    this.observers = observers;
     }

  public void start() {
    if (started.compareAndSet(false, true)){
      startNewHeightManager(blockchain.getChainHeadHeader());
    }
  }

  private void agent() {
        System.out.println("===================================================fucking hello");

          try {
                final BlockHeader currentMiningParent = currentHeightManager.getParentBlockHeader();
                final long newChainHeight = currentHeightManager.getChainHeight();

                String parentBlockNumber = String.valueOf(currentMiningParent.getNumber());
                String txPool = transactionPool.getAgentPendingTransactions().toString();
                out.writeUTF(parentBlockNumber + ":" + txPool + ":" + out + ":" + "Check");

             final IbftBlockCreator blockCreator =
                    ibftBlockCreatorFactory.AgentCreate(currentMiningParent, 0, transactionPool.getPendingTransactions());
            System.out.println("========================================================"+newChainHeight);


            final Block block = blockCreator.createBlock(currentMiningParent.getTimestamp()+1);

            observers.forEach(obs ->obs.agentBlockMined(block));

            System.out.println("============================================" + in.readUTF());


       /*         while (true){
                  String data = in.readUTF();
                  if (data.contains("FirstResult")) {
                    //Clock clock = ibftFinalState.getClock();
                    System.out.println("============================================" + in.readUTF());
                    out.writeUTF("hello fucking agent");
                    final IbftBlockCreator blockCreator =
                            ibftBlockCreatorFactory.AgentCreate(currentMiningParent, 0, transactionPool.getPendingTransactions());


                    final Block block = blockCreator.createBlock(currentMiningParent.getTimestamp()+1);

                    System.out.println("========================================================"+block.getHeader().toString());
                    System.out.println("========================================================"+block.getBody().toString());
                    System.out.println("========================================================"+newChainHeight);

                    observers.forEach(obs ->obs.agentBlockMined(block));



                  }

                  if (data.contains("FirstBlock")){

                    System.out.println("=========================================" + in.readUTF());


                  }
                }*/




          }catch (Exception e) {
            LOG.error(e); }

        }


          //System.out.println("============================================" + in.readUTF());

          //System.out.println("++++++++++++++++++++++" + transactionPool.getAgentPendingTransactions());

          //ibftFinalState.getBlockCreatorFactory().create(currentMiningParent, parentBlockNumber).createBlock(timestamp[])
          //IbftBlockCreator blockCreator = new IbftBlockCreator();





    public void handleMessageEvent(final IbftReceivedMessageEvent msg) {
      final MessageData data = msg.getMessage().getData();
      if (!duplicateMessageTracker.hasSeenMessage(data)) {
        duplicateMessageTracker.addSeenMessage(data);
        handleMessage(msg.getMessage());
      }else {
        LOG.trace("Discarded duplicate message");
      }
    }


  private void handleMessage(final Message message) {
    final MessageData messageData = message.getData();
    if (agentStatus == 1 || agentStatus == 2) {
      agent();
    } else {
      switch (messageData.getCode()) {
        case IbftV2.PROPOSAL:
          consumeMessage(
                  message,
                  ProposalMessageData.fromMessageData(messageData).decode(),
                  currentHeightManager::handleProposalPayload);
          break;

        case IbftV2.PREPARE:
          consumeMessage(
                  message,
                  PrepareMessageData.fromMessageData(messageData).decode(),
                  currentHeightManager::handlePreparePayload);
          break;

        case IbftV2.COMMIT:
          consumeMessage(
                  message,
                  CommitMessageData.fromMessageData(messageData).decode(),
                  currentHeightManager::handleCommitPayload);
          break;

        case IbftV2.ROUND_CHANGE:
          consumeMessage(
                  message,
                  RoundChangeMessageData.fromMessageData(messageData).decode(),
                  currentHeightManager::handleRoundChangePayload);
          break;

        default:
          throw new IllegalArgumentException(
                  String.format(
                          "Received message with messageCode=%d does not conform to any recognised IBFT message structure",
                          message.getData().getCode()));
      }
    }
  }




  private <P extends IbftMessage<?>> void consumeMessage(
      final Message message, final P ibftMessage, final Consumer<P> handleMessage) {
    LOG.trace("Received IBFT {} message", ibftMessage.getClass().getSimpleName());
    if (processMessage(ibftMessage, message)) {
      gossiper.send(message);
      handleMessage.accept(ibftMessage);
    }
  }

  public void handleNewBlockEvent(final NewChainHead newChainHead) {
    final BlockHeader newBlockHeader = newChainHead.getNewChainHeadHeader();
    final BlockHeader currentMiningParent = currentHeightManager.getParentBlockHeader();
    LOG.debug(
        "New chain head detected (block number={})," + " currently mining on top of {}.",
        newBlockHeader.getNumber(),
        currentMiningParent.getNumber());
    if (newBlockHeader.getNumber() < currentMiningParent.getNumber()) {
      LOG.trace(
          "Discarding NewChainHead event, was for previous block height. chainHeight={} eventHeight={}",
          currentMiningParent.getNumber(),
          newBlockHeader.getNumber());
      return;
    }

    if (newBlockHeader.getNumber() == currentMiningParent.getNumber()) {
      if (newBlockHeader.getHash().equals(currentMiningParent.getHash())) {
        LOG.trace(
            "Discarding duplicate NewChainHead event. chainHeight={} newBlockHash={} parentBlockHash={}",
            newBlockHeader.getNumber(),
            newBlockHeader.getHash(),
            currentMiningParent.getHash());
      } else {
        LOG.error(
            "Subsequent NewChainHead event at same block height indicates chain fork. chainHeight={}",
            currentMiningParent.getNumber());
      }
      return;
    }
    startNewHeightManager(newBlockHeader);
  }

  public void handleBlockTimerExpiry(final BlockTimerExpiry blockTimerExpiry) {
    final ConsensusRoundIdentifier roundIndentifier = blockTimerExpiry.getRoundIndentifier();
    if (isMsgForCurrentHeight(roundIndentifier)) {
      currentHeightManager.handleBlockTimerExpiry(roundIndentifier);
    } else {
      LOG.trace(
          "Block timer event discarded as it is not for current block height chainHeight={} eventHeight={}",
          currentHeightManager.getChainHeight(),
          roundIndentifier.getSequenceNumber());
    }
  }

  public void handleRoundExpiry(final RoundExpiry roundExpiry) {
    if (isMsgForCurrentHeight(roundExpiry.getView())) {
      currentHeightManager.roundExpired(roundExpiry);
    } else {
      LOG.trace(
          "Round expiry event discarded as it is not for current block height chainHeight={} eventHeight={}",
          currentHeightManager.getChainHeight(),
          roundExpiry.getView().getSequenceNumber());
    }
  }

  private void startNewHeightManager(final BlockHeader parentHeader) {
      currentHeightManager = ibftBlockHeightManagerFactory.create(parentHeader);
      final long newChainHeight = currentHeightManager.getChainHeight();
      futureMessageBuffer.retrieveMessagesForHeight(newChainHeight).forEach(this::handleMessage);
    }

  private boolean processMessage(final IbftMessage<?> msg, final Message rawMsg) {
    final ConsensusRoundIdentifier msgRoundIdentifier = msg.getRoundIdentifier();
    if (isMsgForCurrentHeight(msgRoundIdentifier)) {
      return isMsgFromKnownValidator(msg) && ibftFinalState.isLocalNodeValidator();
    } else if (isMsgForFutureChainHeight(msgRoundIdentifier)) {
      LOG.trace("Received message for future block height round={}", msgRoundIdentifier);
      futureMessageBuffer.addMessage(msgRoundIdentifier.getSequenceNumber(), rawMsg);
      // Notify the synchronizer the transmitting peer must have the parent block to the received
      // message's target height.
      sychronizerUpdater.updatePeerChainState(
          msgRoundIdentifier.getSequenceNumber() - 1L, rawMsg.getConnection());
    } else {
      LOG.trace(
          "IBFT message discarded as it is from a previous block height messageType={} chainHeight={} eventHeight={}",
          msg.getMessageType(),
          currentHeightManager.getChainHeight(),
          msgRoundIdentifier.getSequenceNumber());
    }
    return false;
  }

  private boolean isMsgFromKnownValidator(final Authored msg) {
    return ibftFinalState.getValidators().contains(msg.getAuthor());
  }

  private boolean isMsgForCurrentHeight(final ConsensusRoundIdentifier roundIdentifier) {
    return roundIdentifier.getSequenceNumber() == currentHeightManager.getChainHeight();
  }

  private boolean isMsgForFutureChainHeight(final ConsensusRoundIdentifier roundIdentifier) {
    return roundIdentifier.getSequenceNumber() > currentHeightManager.getChainHeight();
  }


}
