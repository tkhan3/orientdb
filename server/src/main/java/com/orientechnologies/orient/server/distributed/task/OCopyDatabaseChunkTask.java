/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.server.distributed.task;

import com.orientechnologies.common.io.OFileUtils;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.distributed.ODistributedDatabaseChunk;
import com.orientechnologies.orient.server.distributed.ODistributedServerLog;
import com.orientechnologies.orient.server.distributed.ODistributedServerManager;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Ask for a database chunk.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public class OCopyDatabaseChunkTask extends OAbstractReplicatedTask {
  private static final long serialVersionUID = 1L;

  private String            fileName;
  private int               chunkNum;
  private long              offset;

  public OCopyDatabaseChunkTask() {
  }

  public OCopyDatabaseChunkTask(final String iFileName, final int iChunkNum, final long iOffset) {
    fileName = iFileName;
    chunkNum = iChunkNum;
    offset = iOffset;
  }

  @Override
  public Object execute(final OServer iServer, ODistributedServerManager iManager, final ODatabaseDocumentTx database)
      throws Exception {
    final File f = new File(fileName);
    if (!f.exists())
      throw new IllegalArgumentException("File name '" + fileName + "' not found");

    final ODistributedDatabaseChunk result = new ODistributedDatabaseChunk(0, f, offset, ODeployDatabaseTask.CHUNK_MAX_SIZE);

    ODistributedServerLog.info(this, iManager.getLocalNodeName(), getNodeSource(), ODistributedServerLog.DIRECTION.OUT,
        "- transferring chunk #%d offset=%d size=%s...", chunkNum, result.offset, OFileUtils.getSizeAsNumber(result.buffer.length));

    return result;
  }

  @Override
  public boolean isRequireNodeOnline() {
    return false;
  }

  @Override
  public RESULT_STRATEGY getResultStrategy() {
    return RESULT_STRATEGY.ANY;
  }

  @Override
  public QUORUM_TYPE getQuorumType() {
    return QUORUM_TYPE.NONE;
  }

  @Override
  public String getPayload() {
    return null;
  }

  @Override
  public String getName() {
    return "copy_db_chunk";
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeUTF(fileName);
    out.writeInt(chunkNum);
    out.writeLong(offset);
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    fileName = in.readUTF();
    chunkNum = in.readInt();
    offset = in.readLong();
  }
}
