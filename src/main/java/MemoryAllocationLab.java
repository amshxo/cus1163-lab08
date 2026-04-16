import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

    static class MemoryBlock {
        int start;
        int size;
        String processName;  // null if free

        public MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        public boolean isFree() {
            return processName == null;
        }

        public int getEnd() {
            return start + size - 1;
        }
    }

    static int totalMemory;
    static ArrayList<MemoryBlock> memory;
    static int successfulAllocations = 0;
    static int failedAllocations = 0;

    public static void processRequests(String filename) {
        memory = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            String firstLine = reader.readLine();
            totalMemory = Integer.parseInt(firstLine);

            memory.add(new MemoryBlock(0, totalMemory, null));

            System.out.println("Total Memory: " + totalMemory + " KB");
            System.out.println("----------------------------------------");
            System.out.println();
            System.out.println("Processing requests...");
            System.out.println();

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(" ");

                if (parts[0].equals("REQUEST")) {

                    String processName = parts[1];
                    int size = Integer.parseInt(parts[2]);

                    allocate(processName, size);

                } else if (parts[0].equals("RELEASE")) {

                    String processName = parts[1];

                    boolean found = false;

                    for (MemoryBlock block : memory) {

                        if (!block.isFree() && block.processName.equals(processName)) {

                            block.processName = null;
                            found = true;

                            System.out.println("RELEASE " + processName + " → SUCCESS");
                            break;
                        }
                    }

                    if (!found) {
                        System.out.println("RELEASE " + processName + " → FAILED");
                    }
                }
            }

            reader.close();

        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
    }

    private static void allocate(String processName, int size) {

        for (int i = 0; i < memory.size(); i++) {

            MemoryBlock block = memory.get(i);

            if (block.isFree() && block.size >= size) {

                if (block.size > size) {

                    int remainingSize = block.size - size;
                    int newStart = block.start + size;

                    block.size = size;
                    block.processName = processName;

                    MemoryBlock newBlock = new MemoryBlock(newStart, remainingSize, null);

                    memory.add(i + 1, newBlock);

                } else {

                    block.processName = processName;

                }

                successfulAllocations++;

                System.out.println("REQUEST " + processName + " " + size + " KB → SUCCESS");

                return;
            }
        }

        failedAllocations++;

        System.out.println("REQUEST " + processName + " " + size + " KB → FAILED");
    }

    public static void displayStatistics() {
        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        int blockNum = 1;
        for (MemoryBlock block : memory) {
            String status = block.isFree() ? "FREE" : block.processName;
            String allocated = block.isFree() ? "" : " - ALLOCATED";
            System.out.printf("Block %d: [%d-%d]%s%s (%d KB)%s\n",
                    blockNum++,
                    block.start,
                    block.getEnd(),
                    " ".repeat(Math.max(1, 10 - String.valueOf(block.getEnd()).length())),
                    status,
                    block.size,
                    allocated);
        }

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");

        int allocatedMem = 0;
        int freeMem = 0;
        int numProcesses = 0;
        int numFreeBlocks = 0;
        int largestFree = 0;

        for (MemoryBlock block : memory) {
            if (block.isFree()) {
                freeMem += block.size;
                numFreeBlocks++;
                largestFree = Math.max(largestFree, block.size);
            } else {
                allocatedMem += block.size;
                numProcesses++;
            }
        }

        double allocatedPercent = (allocatedMem * 100.0) / totalMemory;
        double freePercent = (freeMem * 100.0) / totalMemory;
        double fragmentation = freeMem > 0 ?
                ((freeMem - largestFree) * 100.0) / freeMem : 0;

        System.out.printf("Total Memory:           %d KB\n", totalMemory);
        System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocatedMem, allocatedPercent);
        System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePercent);
        System.out.printf("Number of Processes:    %d\n", numProcesses);
        System.out.printf("Number of Free Blocks:  %d\n", numFreeBlocks);
        System.out.printf("Largest Free Block:     %d KB\n", largestFree);
        System.out.printf("External Fragmentation: %.2f%%\n", fragmentation);

        System.out.println("\nSuccessful Allocations: " + successfulAllocations);
        System.out.println("Failed Allocations:     " + failedAllocations);
        System.out.println("========================================");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MemoryAllocationLab <input_file>");
            System.out.println("Example: java MemoryAllocationLab memory_requests.txt");
            return;
        }

        System.out.println("========================================");
        System.out.println("Memory Allocation Simulator (First-Fit)");
        System.out.println("========================================\n");
        System.out.println("Reading from: " + args[0]);

        processRequests(args[0]);
        displayStatistics();
    }
}
