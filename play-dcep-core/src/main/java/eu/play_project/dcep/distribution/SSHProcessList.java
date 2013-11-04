package eu.play_project.dcep.distribution;

import org.objectweb.proactive.core.process.AbstractListProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.ssh.SSHProcess;

public class SSHProcessList extends AbstractListProcessDecorator {

	private static final long serialVersionUID = 657567819810497094L;

	public SSHProcessList() {
        super();
    }

    /**
     * @see org.objectweb.proactive.core.process.AbstractListProcessDecorator#createProcess()
     */
    @Override
    protected ExternalProcessDecorator createProcess() {
        return new SSHProcess();
    }
}