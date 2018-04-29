package org.pitest.maven;

import org.apache.maven.scm.ScmFileStatus;

/**
 * Maps string to maven ScmFileStatus constants.
 *
 * Descriptions copied from maven scm source.
 */
public enum ScmStatus {

  /**
   * File is added to the working tree and does not yet exist in the repository
   */
  ADDED(ScmFileStatus.ADDED),

  /**
   * File is removed from the working tree thus not revisioned anymore.<br>
   * The file is still present in the repository.<br>
   * The file could be deleted from the filesystem depending on the provider.
   */
  DELETED(ScmFileStatus.DELETED),

  /**
   * The file has been modified in the working tree.
   */
  MODIFIED(ScmFileStatus.MODIFIED),

  /**
   * The file has been renamed or moved in the working tree.
   */
  RENAMED(ScmFileStatus.RENAMED),

  /**
   * The file has been copied in the working tree.
   */
  COPIED(ScmFileStatus.COPIED),

  /**
   * The file is missing in the working tree.
   */
  MISSING(ScmFileStatus.MISSING),

  /**
   * File from working tree is checked into the repository
   */
  CHECKED_IN(ScmFileStatus.CHECKED_IN),

  /**
   * File is checked out from the repository and into the working tree
   */
  CHECKED_OUT(ScmFileStatus.CHECKED_OUT),

  /**
   * The file in the working tree has differences to the one in repository that
   * conflicts ie. it cannot automatically be merged.
   */
  CONFLICT(ScmFileStatus.CONFLICT),

  /**
   * The file in the working tree has been updated with changes from the
   * repository.
   */
  PATCHED(ScmFileStatus.PATCHED),

  /**
   * The file is added, removed or updated from the repository, thus its
   * up-to-date with the version in the repository. See also isUpdate()
   */
  UPDATED(ScmFileStatus.UPDATED),

  /**
   * The file is part of a tag.
   */
  TAGGED(ScmFileStatus.TAGGED),

  /**
   * The file is locked.
   */
  LOCKED(ScmFileStatus.LOCKED),

  /**
   * The file is in the working tree but is not versioned and not ignored
   * either.
   */
  UNKNOWN(ScmFileStatus.UNKNOWN),

  /**
   * The file is being edited
   */
  EDITED(ScmFileStatus.EDITED);

  private final ScmFileStatus status;

  public ScmFileStatus getStatus() {
    return this.status;
  }

  ScmStatus(final ScmFileStatus status) {
    this.status = status;
  }

}
