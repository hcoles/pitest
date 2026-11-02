/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.util;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class SocketFinder {

  public static Path pathOf(final ServerSocketChannel channel) {
    try {
      return ((UnixDomainSocketAddress) channel.getLocalAddress()).getPath();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }


  public synchronized ServerSocketChannel getNextAvailableServerSocket() {
    try {
      final Path dir = Files.createTempDirectory("pit");
      final Path path = dir.resolve("s");
      final ServerSocketChannel channel = ServerSocketChannel
          .open(StandardProtocolFamily.UNIX);
      channel.bind(UnixDomainSocketAddress.of(path));
      return channel;
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

}
