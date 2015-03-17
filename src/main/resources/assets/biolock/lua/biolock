--[[
biolock
Shell command for basic working with the BioLock 
biometric scanner peripheral.

Code by GopherAtl.
Do whatever you want with this code, unless you 
want to take credit for it, in which case, screw 
you, dick.

--]]

local args={...}

if #args<2 then
  print([[Usage:
biolock <side> <command> <args...>
<side> is the side of the computer where the biolock you want to access is.
<command> is the command to perform with the biolock, and args are the arguments to that command.
commands:
learn - waits for a print to scan, saves the print and saves it with a provided name and access level. Args: <name> <accesslevel>
forget - forgets a stored name. Args: <name>
list - lists all prints, names, and access levels stored in the biolock. No arguments.
]])
  return
end

local side=args[1]
local cmd=args[2];

if peripheral.getType(side)~="biolock" then
  print("No BioLock on that side!")
  return
end

local bio=peripheral.wrap(side)

if cmd=="learn" then
  if #args<4 then
    print("Insufficient arguments. Syntax:\nbiolock <side> learn <username> <accesslevel>")
    return
  end
  
  print("scan the person you want to the block to learn..")
  local _,prnt=os.pullEvent("biolock")
  local succ,err=bio.learn(args[3],prnt,tonumber(args[4])  )
  if succ then
    print("Bioprint learned!")
  else
    print("Error learning bioprint: "..err)
  end
elseif cmd=="forget" then
  if #args<3 then
    print("Insufficient arguments. Syntax:\nbiolock <side> forget <username>")
    return
  end
  
  local succ,err=bio.forget(args[3])
  if succ then
    print("Bioprint forgotten!")
  else
    print("Error forgetting bioprint : "..err)
  end
elseif cmd=="list" then
  local minLevel=tonumber(args[3]) or 1
  local nameList={bio.getLearnedNames(minLevel)}
  print("  Biometric Signature           Level Name")
  for i=1,#nameList do
    local name=nameList[i]
    print(bio.getPrint(name).."   "..bio.getAccessLevel(name).."   "..name)
  end
  print(#nameList.." stored prints")  
end
  
  
  
    